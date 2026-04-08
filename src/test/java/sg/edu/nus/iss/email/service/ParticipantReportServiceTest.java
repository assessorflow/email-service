package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.nus.iss.email.dto.participant_report.ParticipantReportEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantReportServiceTest {

    @Mock private EmailLogRepository emailLogRepository;
    @Mock private EmailSenderService emailSenderService;
    @Mock private PubSubTemplate pubSubTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private ParticipantReportService service;

    @Test
    void handleRequest_success() throws Exception {
        ParticipantReportEvent event = ParticipantReportEvent.builder()
                .workflowId("wf_123")
                .assessmentId("a1b2c3d4-0000-0000-0000-000000000000")
                .participantEmail("student@test.com")
                .participantName("Alice")
                .assessmentName("OOP Quiz")
                .reportId("r1b2c3d4-0000-0000-0000-000000000000")
                .reportLink("https://app.assessorflow.com/report/123")
                .score("85%")
                .build();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "student@test.com", EmailLog.EmailType.PARTICIPANT_REPORT))
                .thenReturn(false);
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> i.getArgument(0));
        when(emailSenderService.renderTemplate(eq("participant-report"), anyMap())).thenReturn("<html>rendered</html>");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(pubSubTemplate.publish(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture("msg-id"));

        service.handleRequest(event);

        verify(emailLogRepository).save(any(EmailLog.class));
        verify(emailSenderService).renderTemplate(eq("participant-report"), anyMap());
        verify(pubSubTemplate).publish(anyString(), anyString());
    }

    @Test
    void handleRequest_duplicate_ignored() {
        ParticipantReportEvent event = ParticipantReportEvent.builder()
                .workflowId("wf_123")
                .participantEmail("student@test.com")
                .build();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "student@test.com", EmailLog.EmailType.PARTICIPANT_REPORT))
                .thenReturn(true);

        service.handleRequest(event);

        verify(emailLogRepository, never()).save(any());
        verify(pubSubTemplate, never()).publish(anyString(), anyString());
    }
}
