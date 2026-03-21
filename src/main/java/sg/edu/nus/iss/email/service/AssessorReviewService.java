package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.time.LocalDateTime;
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

    /**
     * Stage 1 — Request: persist QUEUED log, set emailLogId, publish to deliver topic.
     */
    @Transactional
    public void handleRequest(AssessorReviewEvent event) {
        EmailLog emailLog = EmailLog.builder()
                .workflowId(event.getWorkflowId())
                .recipientEmail(event.getRecipientEmail())
                .emailType(EmailLog.EmailType.QUESTION_REVIEW)
                .subject(event.getSubject())
                .status(EmailLog.Status.QUEUED)
                .build();
        emailLogRepository.save(emailLog);

        event.setEmailLogId(emailLog.getId().toString());

        try {
            String payload = objectMapper.writeValueAsString(event);
            pubSubTemplate.publish(deliverTopic, payload).get();
            log.info("[ASSESSOR_REVIEW][REQUEST] Persisted and published to deliver: logId={}", emailLog.getId());
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage("Failed to publish to deliver topic: " + e.getMessage());
            emailLogRepository.save(emailLog);
            throw new RuntimeException("Failed to publish deliver job", e);
        }
    }

    /**
     * Stage 2 — Deliver: lookup EmailLog, render template, send via SES, update status.
     */
    @Transactional
    public void handleDeliver(AssessorReviewEvent event) {
        EmailLog emailLog = null;
        if (event.getEmailLogId() != null) {
            emailLog = emailLogRepository.findById(
                    UUID.fromString(event.getEmailLogId())).orElse(null);
        }

        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("assessorName", event.getAssessorName());
            templateData.put("assessmentName", event.getAssessmentName());
            templateData.put("reviewLink", event.getReviewLink());

            emailSenderService.sendHtmlEmail(
                    event.getRecipientEmail(),
                    event.getSubject(),
                    "question-review",
                    templateData
            );

            if (emailLog != null) {
                emailLog.setStatus(EmailLog.Status.SENT);
                emailLog.setSentAt(LocalDateTime.now());
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
