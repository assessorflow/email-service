package sg.edu.nus.iss.email.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sg.edu.nus.iss.email.dto.assessment_link.AssessmentLinkEvent;
import sg.edu.nus.iss.email.service.AssessmentLinkService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssessmentLinkSubscriber {

    private final AssessmentLinkService service;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "assessmentLinkRequestChannel")
    public void handleRequest(Message<String> message) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            AssessmentLinkEvent event = objectMapper.readValue(message.getPayload(), AssessmentLinkEvent.class);
            log.info("[ASSESSMENT_LINK][REQUEST] Received: recipient={}", event.getRecipientEmail());
            service.handleRequest(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[ASSESSMENT_LINK][REQUEST] Failed to process event", e);
            if (ack != null) ack.nack();
        }
    }

    @ServiceActivator(inputChannel = "assessmentLinkDeliverChannel")
    public void handleDeliver(Message<String> message) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            AssessmentLinkEvent event = objectMapper.readValue(message.getPayload(), AssessmentLinkEvent.class);
            log.info("[ASSESSMENT_LINK][DELIVER] Sending email: recipient={}", event.getRecipientEmail());
            service.handleDeliver(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[ASSESSMENT_LINK][DELIVER] Failed to deliver email", e);
            if (ack != null) ack.nack();
        }
    }
}
