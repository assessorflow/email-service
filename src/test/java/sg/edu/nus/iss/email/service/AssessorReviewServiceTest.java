package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessorReviewServiceTest {

    @Mock private EmailLogRepository emailLogRepository;
    @Mock private EmailSenderService emailSenderService;
    @Mock private PubSubTemplate pubSubTemplate;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private AssessorReviewService service;

    @Test
    void handleRequest_success() throws Exception {
        AssessorReviewEvent event = AssessorReviewEvent.builder()
                .workflowId("wf_123")
                .assessmentId("a1b2c3d4-0000-0000-0000-000000000000")
                .assessorEmail("assessor@test.com")
                .assessorName("Test Assessor")
                .assessmentName("OOP Quiz")
                .reviewLink("https://app.assessorflow.com/review/123")
                .questionCount(8)
                .build();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "assessor@test.com", EmailLog.EmailType.ASSESSOR_REVIEW))
                .thenReturn(false);
        when(emailLogRepository.save(any(EmailLog.class))).thenAnswer(i -> i.getArgument(0));
        when(emailSenderService.renderTemplate(eq("question-review"), anyMap())).thenReturn("<html>rendered</html>");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(pubSubTemplate.publish(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture("msg-id"));

        service.handleRequest(event);

        verify(emailLogRepository).save(any(EmailLog.class));
        verify(emailSenderService).renderTemplate(eq("question-review"), anyMap());
        verify(pubSubTemplate).publish(anyString(), anyString());
    }

    @Test
    void handleRequest_duplicate_ignored() {
        AssessorReviewEvent event = AssessorReviewEvent.builder()
                .workflowId("wf_123")
                .assessorEmail("assessor@test.com")
                .build();

        when(emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
                "wf_123", "assessor@test.com", EmailLog.EmailType.ASSESSOR_REVIEW))
                .thenReturn(true);

        service.handleRequest(event);

        verify(emailLogRepository, never()).save(any());
        verify(pubSubTemplate, never()).publish(anyString(), anyString());
    }
}
