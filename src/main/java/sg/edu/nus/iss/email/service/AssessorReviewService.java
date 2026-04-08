package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.email.dto.EmailDeliverEvent;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessorReviewService {

    private final EmailLogRepository emailLogRepository;
    private final EmailSenderService emailSenderService;
    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.pubsub.assessor-review.deliver-topic}")
    private String deliverTopic;

    @Transactional
    public void handleRequest(AssessorReviewEvent event) {
        if (emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                event.getWorkflowId(), event.getAssessorEmail(), EmailLog.EmailType.ASSESSOR_REVIEW)) {
            log.warn("[ASSESSOR_REVIEW][REQUEST] Duplicate ignored: workflow={}, recipient={}",
                    event.getWorkflowId(), event.getAssessorEmail());
            return;
        }

        String subject = "Questions Ready for Review — " + event.getAssessmentName();

        EmailLog emailLog = EmailLog.builder()
                .workflowId(event.getWorkflowId())
                .assessmentId(event.getAssessmentId() != null ? UUID.fromString(event.getAssessmentId()) : null)
                .recipientEmail(event.getAssessorEmail())
                .emailType(EmailLog.EmailType.ASSESSOR_REVIEW)
                .subject(subject)
                .status(EmailLog.Status.QUEUED)
                .build();
        emailLogRepository.save(emailLog);

        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("assessorName", event.getAssessorName());
            templateData.put("assessmentName", event.getAssessmentName());
            templateData.put("reviewLink", event.getReviewLink());

            String renderedHtml = emailSenderService.renderTemplate("question-review", templateData);

            EmailDeliverEvent deliverEvent = EmailDeliverEvent.builder()
                    .emailLogId(emailLog.getId().toString())
                    .recipientEmail(event.getAssessorEmail())
                    .subject(subject)
                    .templateId("assessor_review_v1")
                    .renderedHtml(renderedHtml)
                    .build();

            String payload = objectMapper.writeValueAsString(deliverEvent);
            pubSubTemplate.publish(deliverTopic, payload).get();
            log.info("[ASSESSOR_REVIEW][REQUEST] Rendered and published to deliver: logId={}", emailLog.getId());
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage("Failed to render/publish: " + e.getMessage());
            emailLogRepository.save(emailLog);
            throw new RuntimeException("Failed to publish deliver job", e);
        }
    }

    @Transactional
    public void handleDeliver(EmailDeliverEvent event) {
        EmailLog emailLog = null;
        if (event.getEmailLogId() != null) {
            emailLog = emailLogRepository.findById(UUID.fromString(event.getEmailLogId())).orElse(null);
        }

        try {
            emailSenderService.sendRenderedHtml(
                    event.getRecipientEmail(),
                    event.getSubject(),
                    event.getRenderedHtml()
            );

            if (emailLog != null) {
                emailLog.setStatus(EmailLog.Status.SENT);
                emailLog.setSentAt(Instant.now());
                emailLogRepository.save(emailLog);
            }
            log.info("[ASSESSOR_REVIEW][DELIVER] Email sent to {}", event.getRecipientEmail());
        } catch (Exception e) {
            if (emailLog != null) {
                emailLog.setStatus(EmailLog.Status.FAILED);
                emailLog.setErrorMessage(e.getMessage());
                emailLogRepository.save(emailLog);
            }
            throw new RuntimeException("Failed to send assessor review email via SES", e);
        }
    }
}
