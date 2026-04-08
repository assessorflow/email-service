package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.nus.iss.email.dto.assessment_link.AssessmentLinkEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentLinkServiceTest {

    @Mock private EmailLogRepository emailLogRepository;
    @Mock private EmailSenderService emailSenderService;
    @Mock private PubSubTemplate pubSubTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private AssessmentLinkService service;

    @Test
    void handleRequest_success() throws Exception {
        AssessmentLinkEvent event = AssessmentLinkEvent.builder()
                .workflowId("wf_123")
                .assessmentId("a1b2c3d4-0000-0000-0000-000000000000")
                .participantEmail("student@test.com")
                .participantName("Alice")
                .assessmentName("OOP Quiz")
                .assessmentLink("https://app.assessorflow.com/assessment/123")
                .durationMinutes(60)
                .deadline("2026-04-15T23:59:00Z")
                .build();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "student@test.com", EmailLog.EmailType.PARTICIPANT_INVITATION))
                .thenReturn(false);
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> i.getArgument(0));
        when(emailSenderService.renderTemplate(eq("assessment-link"), anyMap())).thenReturn("<html>rendered</html>");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(pubSubTemplate.publish(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture("msg-id"));

        service.handleRequest(event);

        verify(emailLogRepository).save(any(EmailLog.class));
        verify(emailSenderService).renderTemplate(eq("assessment-link"), anyMap());
        verify(pubSubTemplate).publish(anyString(), anyString());
    }

    @Test
    void handleRequest_duplicate_ignored() {
        AssessmentLinkEvent event = AssessmentLinkEvent.builder()
                .workflowId("wf_123")
                .participantEmail("student@test.com")
                .build();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "student@test.com", EmailLog.EmailType.PARTICIPANT_INVITATION))
                .thenReturn(true);

        service.handleRequest(event);

        verify(emailLogRepository, never()).save(any());
        verify(pubSubTemplate, never()).publish(anyString(), anyString());
    }
}
