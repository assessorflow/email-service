
package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import sg.edu.nus.iss.email.dto.EmailDeliverEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
public abstract class AbstractEmailService<T> {

    protected final EmailLogRepository emailLogRepository;
    protected final EmailSenderService emailSenderService;
    protected final PubSubTemplate pubSubTemplate;
    protected final ObjectMapper objectMapper;
    protected final MeterRegistry meterRegistry;

    protected AbstractEmailService(EmailLogRepository emailLogRepository,
                                   EmailSenderService emailSenderService,
                                   PubSubTemplate pubSubTemplate,
                                   ObjectMapper objectMapper,
                                   MeterRegistry meterRegistry) {
        this.emailLogRepository = emailLogRepository;
        this.emailSenderService = emailSenderService;
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    protected abstract String getRecipientEmail(T event);
    protected abstract String getWorkflowId(T event);
    protected abstract String getAssessmentId(T event);
    protected abstract EmailLog.EmailType getEmailType();
    protected abstract String getTemplateName();
    protected abstract String getDeliverTopic();
    protected abstract Map<String, Object> buildTemplateData(T event);
    protected abstract String buildSubject(T event);

    public void handleRequest(T event) {
        String recipientEmail = getRecipientEmail(event);
        String workflowId = getWorkflowId(event);
        String tag = getEmailType().name();

        if (emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                workflowId, recipientEmail, getEmailType())) {
            log.warn("[{}][REQUEST] Duplicate ignored: workflow={}", tag, workflowId);
            return;
        }

        String subject = buildSubject(event);

        EmailLog emailLog = EmailLog.builder()
                .workflowId(workflowId)
                .assessmentId(parseUuid(getAssessmentId(event)))
                .recipientEmail(recipientEmail)
                .emailType(getEmailType())
                .subject(subject)
                .status(EmailLog.Status.QUEUED)
                .build();
        emailLogRepository.save(emailLog);

        try {
            String renderedHtml = emailSenderService.renderTemplate(getTemplateName(), buildTemplateData(event));

            EmailDeliverEvent deliverEvent = EmailDeliverEvent.builder()
                    .emailLogId(emailLog.getId().toString())
                    .recipientEmail(recipientEmail)
                    .subject(subject)
                    .renderedHtml(renderedHtml)
                    .build();

            String payload = objectMapper.writeValueAsString(deliverEvent);
            pubSubTemplate.publish(getDeliverTopic(), payload).get();
            meterRegistry.counter("email.request.published", "type", tag).increment();
            log.info("[{}][REQUEST] Rendered and published: logId={}", tag, emailLog.getId());
        } catch (Exception e) {
            emailLog.setStatus(EmailLog.Status.FAILED);
            emailLog.setErrorMessage("Failed to render/publish: " + e.getMessage());
            emailLogRepository.save(emailLog);
            meterRegistry.counter("email.request.failed", "type", tag).increment();
            throw new RuntimeException("Failed to publish deliver job", e);
        }
    }

    public void handleDeliver(EmailDeliverEvent event) {
        String tag = getEmailType().name();
        EmailLog emailLog = null;

        if (event.getEmailLogId() != null) {
            emailLog = emailLogRepository.findById(UUID.fromString(event.getEmailLogId())).orElse(null);
        }

        // Idempotency guard: don't re-send if already delivered
        if (emailLog != null && emailLog.getStatus() == EmailLog.Status.SENT) {
            log.warn("[{}][DELIVER] Already sent, skipping: logId={}", tag, event.getEmailLogId());
            return;
        }

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            emailSenderService.sendRenderedHtml(
                    event.getRecipientEmail(),
                    event.getSubject(),
                    event.getRenderedHtml()
            );
        } catch (Exception e) {
            if (emailLog != null) {
                emailLog.setStatus(EmailLog.Status.FAILED);
                emailLog.setErrorMessage(e.getMessage());
                emailLogRepository.save(emailLog);
            }
            meterRegistry.counter("email.deliver.failed", "type", tag).increment();
            throw new RuntimeException("Failed to send email via SES", e);
        }

        sample.stop(Timer.builder("email.ses.duration").tag("type", tag).register(meterRegistry));

        if (emailLog != null) {
            emailLog.setStatus(EmailLog.Status.SENT);
            emailLog.setSentAt(Instant.now());
            emailLogRepository.save(emailLog);
        }
        meterRegistry.counter("email.deliver.sent", "type", tag).increment();
        log.info("[{}][DELIVER] Email sent: logId={}", tag, event.getEmailLogId());
    }

    private UUID parseUuid(String value) {
        if (value == null || value.isBlank()) return null;
        return UUID.fromString(value);
    }
}