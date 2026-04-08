package sg.edu.nus.iss.email.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sg.edu.nus.iss.email.dto.EmailDeliverEvent;
import sg.edu.nus.iss.email.dto.participant_report.ParticipantReportEvent;
import sg.edu.nus.iss.email.service.ParticipantReportService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ParticipantReportSubscriber {

    private final ParticipantReportService service;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "participantReportRequestChannel")
    public void handleRequest(Message<String> message) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            ParticipantReportEvent event = objectMapper.readValue(message.getPayload(), ParticipantReportEvent.class);
            log.info("[PARTICIPANT_REPORT][REQUEST] Received: recipient={}", event.getParticipantEmail());
            service.handleRequest(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[PARTICIPANT_REPORT][REQUEST] Failed to process event", e);
            if (ack != null) ack.nack();
        }
    }

    @ServiceActivator(inputChannel = "participantReportDeliverChannel")
    public void handleDeliver(Message<String> message) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            EmailDeliverEvent event = objectMapper.readValue(message.getPayload(), EmailDeliverEvent.class);
            log.info("[PARTICIPANT_REPORT][DELIVER] Sending email: recipient={}", event.getRecipientEmail());
            service.handleDeliver(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[PARTICIPANT_REPORT][DELIVER] Failed to deliver email", e);
            if (ack != null) ack.nack();
        }
    }
}
