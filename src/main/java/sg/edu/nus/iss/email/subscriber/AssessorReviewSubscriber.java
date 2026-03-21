package sg.edu.nus.iss.email.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.service.AssessorReviewService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssessorReviewSubscriber {

    private final AssessorReviewService service;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "assessorReviewRequestChannel")
    public void handleRequest(Message<String> message) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            AssessorReviewEvent event = objectMapper.readValue(message.getPayload(), AssessorReviewEvent.class);
            log.info("[ASSESSOR_REVIEW][REQUEST] Received: recipient={}", event.getRecipientEmail());
            service.handleRequest(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[ASSESSOR_REVIEW][REQUEST] Failed to process event", e);
            if (ack != null) ack.nack();
        }
    }

    @ServiceActivator(inputChannel = "assessorReviewDeliverChannel")
    public void handleDeliver(Message<String> message) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            AssessorReviewEvent event = objectMapper.readValue(message.getPayload(), AssessorReviewEvent.class);
            log.info("[ASSESSOR_REVIEW][DELIVER] Sending email: recipient={}", event.getRecipientEmail());
            service.handleDeliver(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[ASSESSOR_REVIEW][DELIVER] Failed to deliver email", e);
            if (ack != null) ack.nack();
        }
    }
}
