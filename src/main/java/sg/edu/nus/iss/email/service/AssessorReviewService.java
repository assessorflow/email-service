package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.Map;

@Service
public class AssessorReviewService extends AbstractEmailService<AssessorReviewEvent> {

    @Value("${app.pubsub.assessor-review.deliver-topic}")
    private String deliverTopic;

    public AssessorReviewService(EmailLogRepository repo, EmailSenderService sender,
                                 PubSubTemplate pubSub, ObjectMapper mapper, MeterRegistry meter) {
        super(repo, sender, pubSub, mapper, meter);
    }

    @Override protected String getRecipientEmail(AssessorReviewEvent e) { return e.getAssessorEmail(); }
    @Override protected String getWorkflowId(AssessorReviewEvent e) { return e.getWorkflowId(); }
    @Override protected String getAssessmentId(AssessorReviewEvent e) { return e.getAssessmentId(); }
    @Override protected EmailLog.EmailType getEmailType() { return EmailLog.EmailType.ASSESSOR_REVIEW; }
    @Override protected String getTemplateName() { return "question-review"; }
    @Override protected String getDeliverTopic() { return deliverTopic; }

    @Override
    protected String buildSubject(AssessorReviewEvent e) {
        return "Questions Ready for Review — " + e.getAssessmentName();
    }

    @Override
    protected Map<String, Object> buildTemplateData(AssessorReviewEvent e) {
        return Map.of(
                "assessorName", e.getAssessorName(),
                "assessmentName", e.getAssessmentName(),
                "reviewLink", e.getReviewLink()
        );
    }
}