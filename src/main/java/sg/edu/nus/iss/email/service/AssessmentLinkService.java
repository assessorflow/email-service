package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.email.dto.assessment_link.AssessmentLinkEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.Map;

@Service
public class AssessmentLinkService extends AbstractEmailService<AssessmentLinkEvent> {

    @Value("${app.pubsub.assessment-link.deliver-topic}")
    private String deliverTopic;

    public AssessmentLinkService(EmailLogRepository repo, EmailSenderService sender,
                                 PubSubTemplate pubSub, ObjectMapper mapper, MeterRegistry meter) {
        super(repo, sender, pubSub, mapper, meter);
    }

    @Override protected String getRecipientEmail(AssessmentLinkEvent e) { return e.getParticipantEmail(); }
    @Override protected String getWorkflowId(AssessmentLinkEvent e) { return e.getWorkflowId(); }
    @Override protected String getAssessmentId(AssessmentLinkEvent e) { return e.getAssessmentId(); }
    @Override protected EmailLog.EmailType getEmailType() { return EmailLog.EmailType.PARTICIPANT_INVITATION; }
    @Override protected String getTemplateName() { return "assessment-link"; }
    @Override protected String getDeliverTopic() { return deliverTopic; }

    @Override
    protected String buildSubject(AssessmentLinkEvent e) {
        return "Your Assessment is Ready — " + e.getAssessmentName();
    }

    @Override
    protected Map<String, Object> buildTemplateData(AssessmentLinkEvent e) {
        return Map.of(
                "participantName", e.getParticipantName(),
                "assessmentName", e.getAssessmentName(),
                "assessmentLink", e.getAssessmentLink()
        );
    }
}