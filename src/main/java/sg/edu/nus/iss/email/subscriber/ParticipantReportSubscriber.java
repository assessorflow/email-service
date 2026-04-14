package sg.edu.nus.iss.email.subscriber;

import lombok.RequiredArgsConstructor;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import sg.edu.nus.iss.email.dto.EmailDeliverEvent;
import sg.edu.nus.iss.email.dto.participant_report.ParticipantReportEvent;
import sg.edu.nus.iss.email.service.ParticipantReportService;

@Component
@RequiredArgsConstructor
public class ParticipantReportSubscriber {

    private final ParticipantReportService service;
    private final PubSubMessageHandler handler;

    @ServiceActivator(inputChannel = "participantReportRequestChannel")
    public void handleRequest(Message<String> message) {
        handler.handle(message, ParticipantReportEvent.class, service::handleRequest, "PARTICIPANT_REPORT][REQUEST");
    }

    @ServiceActivator(inputChannel = "participantReportDeliverChannel")
    public void handleDeliver(Message<String> message) {
        handler.handle(message, EmailDeliverEvent.class, service::handleDeliver, "PARTICIPANT_REPORT][DELIVER");
    }
}