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
import sg.edu.nus.iss.email.dto.participant_report.ParticipantReportEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantReportServiceTest {

    @Mock private EmailLogRepository emailLogRepository;
    @Mock private EmailSenderService emailSenderService;
    @Mock private PubSubTemplate pubSubTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private ParticipantReportService service;

    @BeforeEach
    void setUp() {
        service = new ParticipantReportService(emailLogRepository, emailSenderService, pubSubTemplate, objectMapper, meterRegistry);
    }

    @Test
    void handleRequest_success() throws Exception {
        ParticipantReportEvent event = buildEvent();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "student@test.com", EmailLog.EmailType.PARTICIPANT_REPORT))
                .thenReturn(false);
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> {
            EmailLog log = i.getArgument(0);
            if (log.getId() == null) log.setId(UUID.randomUUID());
            return log;
        });
        when(emailSenderService.renderTemplate(eq("participant-report"), anyMap())).thenReturn("<html>report</html>");
        when(pubSubTemplate.publish(any(), anyString())).thenReturn(CompletableFuture.completedFuture("msg-id"));

        service.handleRequest(event);

        verify(emailLogRepository).save(any(EmailLog.class));
        verify(emailSenderService).renderTemplate(eq("participant-report"), anyMap());
    }

    @Test
    void handleRequest_duplicate_ignored() {
        ParticipantReportEvent event = buildEvent();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "student@test.com", EmailLog.EmailType.PARTICIPANT_REPORT))
                .thenReturn(true);

        service.handleRequest(event);

        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void handleDeliver_success() {
        UUID logId = UUID.randomUUID();
        EmailLog emailLog = EmailLog.builder().id(logId).status(EmailLog.Status.QUEUED).build();
        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(emailLog));

        service.handleDeliver(buildDeliverEvent(logId));

        verify(emailSenderService).sendRenderedHtml(anyString(), anyString(), anyString());
        verify(emailLogRepository).save(argThat(log -> log.getStatus() == EmailLog.Status.SENT));
    }

    @Test
    void handleDeliver_alreadySent_skips() {
        UUID logId = UUID.randomUUID();
        EmailLog emailLog = EmailLog.builder().id(logId).status(EmailLog.Status.SENT).build();
        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(emailLog));

        service.handleDeliver(buildDeliverEvent(logId));

        verify(emailSenderService, never()).sendRenderedHtml(anyString(), anyString(), anyString());
    }

    @Test
    void handleDeliver_sesFailure_savesFailedStatus() {
        UUID logId = UUID.randomUUID();
        EmailLog emailLog = EmailLog.builder().id(logId).status(EmailLog.Status.QUEUED).build();
        when(emailLogRepository.findById(logId)).thenReturn(Optional.of(emailLog));
        doThrow(new RuntimeException("SES timeout")).when(emailSenderService)
                .sendRenderedHtml(anyString(), anyString(), anyString());

        assertThrows(RuntimeException.class, () -> service.handleDeliver(buildDeliverEvent(logId)));

        verify(emailLogRepository).save(argThat(log -> log.getStatus() == EmailLog.Status.FAILED));
    }

    private ParticipantReportEvent buildEvent() {
        return ParticipantReportEvent.builder()
                .workflowId("wf_123")
                .assessmentId("a1b2c3d4-0000-0000-0000-000000000000")
                .participantEmail("student@test.com")
                .participantName("Test Student")
                .assessmentName("OOP Quiz")
                .reportId("rpt_456")
                .reportLink("https://app.assessorflow.com/report/456")
                .score("85/100")
                .build();
    }

    private EmailDeliverEvent buildDeliverEvent(UUID logId) {
        return EmailDeliverEvent.builder()
                .emailLogId(logId.toString())
                .recipientEmail("student@test.com")
                .subject("Test")
                .renderedHtml("<html>test</html>")
                .build();
    }
}
