package sg.edu.nus.iss.email.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.support.BasicAcknowledgeablePubsubMessage;
import com.google.cloud.spring.pubsub.support.GcpPubSubHeaders;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class PubSubMessageHandler {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    /**
     * Parse, validate, and process a Pub/Sub message with proper ACK/NACK handling.
     *
     * @param message  the raw Pub/Sub message
     * @param type     DTO class to deserialize into
     * @param handler  business logic to execute on the parsed event
     * @param tag      log prefix (e.g., "ASSESSOR_REVIEW][REQUEST")
     */
    public <T> void handle(Message<String> message, Class<T> type, Consumer<T> handler, String tag) {
        BasicAcknowledgeablePubsubMessage ack = message.getHeaders()
                .get(GcpPubSubHeaders.ORIGINAL_MESSAGE, BasicAcknowledgeablePubsubMessage.class);
        try {
            T event = objectMapper.readValue(message.getPayload(), type);

            Set<ConstraintViolation<T>> violations = validator.validate(event);
            if (!violations.isEmpty()) {
                log.error("[{}] Invalid payload, ACK to discard: {}", tag, violations);
                if (ack != null) ack.ack();
                return;
            }

            handler.accept(event);
            if (ack != null) ack.ack();
        } catch (Exception e) {
            log.error("[{}] Failed to process", tag, e);
            if (ack != null) ack.nack();
        }
    }
}