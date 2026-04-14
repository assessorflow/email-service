package sg.edu.nus.iss.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.email.dto.participant_report.ParticipantReportEvent;
import sg.edu.nus.iss.email.entity.EmailLog;
import sg.edu.nus.iss.email.repository.EmailLogRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class ParticipantReportService extends AbstractEmailService<ParticipantReportEvent> {

    @Value("${app.pubsub.participant-report.deliver-topic}")
    private String deliverTopic;

    public ParticipantReportService(EmailLogRepository repo, EmailSenderService sender,
                                     PubSubTemplate pubSub, ObjectMapper mapper, MeterRegistry meter) {
        super(repo, sender, pubSub, mapper, meter);
    }

    @Override protected String getRecipientEmail(ParticipantReportEvent e) { return e.getParticipantEmail(); }
    @Override protected String getWorkflowId(ParticipantReportEvent e) { return e.getWorkflowId(); }
    @Override protected String getAssessmentId(ParticipantReportEvent e) { return e.getAssessmentId(); }
    @Override protected EmailLog.EmailType getEmailType() { return EmailLog.EmailType.PARTICIPANT_REPORT; }
    @Override protected String getTemplateName() { return "participant-report"; }
    @Override protected String getDeliverTopic() { return deliverTopic; }

    @Override
    protected String buildSubject(ParticipantReportEvent e) {
        return "Your Assessment Report — " + e.getAssessmentName();
    }

    @Override
    protected Map<String, Object> buildTemplateData(ParticipantReportEvent e) {
        Map<String, Object> data = new HashMap<>();
        data.put("participantName", e.getParticipantName());
        data.put("assessmentName", e.getAssessmentName());
        data.put("score", e.getScore());
        data.put("reportLink", e.getReportLink());
        return data;
    }
}