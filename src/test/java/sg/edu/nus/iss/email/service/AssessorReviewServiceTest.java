package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.nus.iss.email.dto.EmailDeliverEvent;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessorReviewServiceTest {

    @Mock private EmailLogRepository emailLogRepository;
    @Mock private EmailSenderService emailSenderService;
    @Mock private PubSubTemplate pubSubTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private AssessorReviewService service;

    @BeforeEach
    void setUp() {
        service = new AssessorReviewService(emailLogRepository, emailSenderService, pubSubTemplate, objectMapper, meterRegistry);
    }

    // ── Request stage ─────────────────────────────────────────

    @Test
    void handleRequest_success() throws Exception {
        AssessorReviewEvent event = buildEvent();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "assessor@test.com", EmailLog.EmailType.ASSESSOR_REVIEW))
                .thenReturn(false);
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> {
            EmailLog log = i.getArgument(0);
            if (log.getId() == null) {
                // Simulate JPA setting UUID on save
                log.setId(UUID.randomUUID());
            }
            return log;
        });
        when(emailSenderService.renderTemplate(eq("question-review"), anyMap())).thenReturn("<html>rendered</html>");
        when(pubSubTemplate.publish(any(), anyString())).thenReturn(CompletableFuture.completedFuture("msg-id"));

        service.handleRequest(event);

        verify(emailLogRepository).save(any(EmailLog.class));
        verify(emailSenderService).renderTemplate(eq("question-review"), anyMap());
        verify(pubSubTemplate).publish(any(), anyString());
    }

    @Test
    void handleRequest_duplicate_ignored() {
        AssessorReviewEvent event = buildEvent();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "assessor@test.com", EmailLog.EmailType.ASSESSOR_REVIEW))
                .thenReturn(true);

        service.handleRequest(event);

        verify(emailLogRepository, never()).save(any());
        verify(pubSubTemplate, never()).publish(anyString(), anyString());
    }

    @Test
    void handleRequest_renderFailure_savesFailedStatus() throws Exception {
        AssessorReviewEvent event = buildEvent();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(anyString(), anyString(), any()))
                .thenReturn(false);
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> {
            EmailLog log = i.getArgument(0);
            if (log.getId() == null) log.setId(UUID.randomUUID());
            return log;
        });
        when(emailSenderService.renderTemplate(anyString(), anyMap())).thenThrow(new RuntimeException("Template error"));

        assertThrows(RuntimeException.class, () -> service.handleRequest(event));

        // Verify FAILED status was saved (second save call)
        verify(emailLogRepository, times(2)).save(argThat(log -> log.getStatus() == EmailLog.Status.FAILED
                || log.getStatus() == EmailLog.Status.QUEUED));
    }

    // ── Deliver stage ─────────────────────────────────────────

    @Test
    void handleDeliver_success_updatesStatusToSent() {
        UUID logId = UUID.randomUUID();
        EmailLog emailLog = EmailLog.builder()
                .id(logId)
                .status(EmailLog.Status.QUEUED)
                .build();

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(emailLog));

        EmailDeliverEvent event = EmailDeliverEvent.builder()
                .emailLogId(logId.toString())
                .recipientEmail("assessor@test.com")
                .subject("Test")
                .renderedHtml("<html>test</html>")
                .build();

        service.handleDeliver(event);

        verify(emailSenderService).sendRenderedHtml("assessor@test.com", "Test", "<html>test</html>");
        verify(emailLogRepository).save(argThat(log -> log.getStatus() == EmailLog.Status.SENT && log.getSentAt() != null));
    }

    @Test
    void handleDeliver_alreadySent_skips() {
        UUID logId = UUID.randomUUID();
        EmailLog emailLog = EmailLog.builder()
                .id(logId)
                .status(EmailLog.Status.SENT)
                .build();

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(emailLog));

        EmailDeliverEvent event = EmailDeliverEvent.builder()
                .emailLogId(logId.toString())
                .recipientEmail("assessor@test.com")
                .subject("Test")
                .renderedHtml("<html>test</html>")
                .build();

        service.handleDeliver(event);

        verify(emailSenderService, never()).sendRenderedHtml(anyString(), anyString(), anyString());
        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void handleDeliver_sesFailure_savesFailedStatus() {
        UUID logId = UUID.randomUUID();
        EmailLog emailLog = EmailLog.builder()
                .id(logId)
                .status(EmailLog.Status.QUEUED)
                .build();

        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(emailLog));
        doThrow(new RuntimeException("SES timeout")).when(emailSenderService)
                .sendRenderedHtml(anyString(), anyString(), anyString());

        EmailDeliverEvent event = EmailDeliverEvent.builder()
                .emailLogId(logId.toString())
                .recipientEmail("assessor@test.com")
                .subject("Test")
                .renderedHtml("<html>test</html>")
                .build();

        assertThrows(RuntimeException.class, () -> service.handleDeliver(event));

        verify(emailLogRepository).save(argThat(log -> log.getStatus() == EmailLog.Status.FAILED));
    }

    // ── Helpers ───────────────────────────────────────────────

    private AssessorReviewEvent buildEvent() {
        return AssessorReviewEvent.builder()
                .workflowId("wf_123")
                .assessmentId("a1b2c3d4-0000-0000-0000-000000000000")
                .assessorEmail("assessor@test.com")
                .assessorName("Test Assessor")
                .assessmentName("OOP Quiz")
                .reviewLink("https://app.assessorflow.com/review/123")
                .questionCount(8)
                .build();
    }
}