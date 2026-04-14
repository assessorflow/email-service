package sg.edu.nus.iss.email.subscriber;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sg.edu.nus.iss.email.dto.EmailDeliverEvent;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.service.AssessorReviewService;

@Component
@RequiredArgsConstructor
public class AssessorReviewSubscriber {

    private final AssessorReviewService service;
    private final PubSubMessageHandler handler;

    @ServiceActivator(inputChannel = "assessorReviewRequestChannel")
    public void handleRequest(Message<String> message) {
        handler.handle(message, AssessorReviewEvent.class, service::handleRequest, "ASSESSOR_REVIEW][REQUEST");
    }

    @ServiceActivator(inputChannel = "assessorReviewDeliverChannel")
    public void handleDeliver(Message<String> message) {
        handler.handle(message, EmailDeliverEvent.class, service::handleDeliver, "ASSESSOR_REVIEW][DELIVER");
    }
}