package sg.edu.nus.iss.email.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.email.dto.assessor_review.AssessorReviewEvent;
import sg.edu.nus.iss.email.dto.assessment_link.AssessmentLinkEvent;
import sg.edu.nus.iss.email.dto.participant_report.ParticipantReportEvent;

import java.util.Map;

@Profile({"dev", "test"})
@RestController
@RequestMapping("/api/v1/test/email")
@RequiredArgsConstructor
@Slf4j
public class EmailTestController {

    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/assessor-review")
    public ResponseEntity<Map<String, String>> sendAssessorReview(@RequestBody AssessorReviewEvent event) throws Exception {
        String payload = objectMapper.writeValueAsString(event);
        pubSubTemplate.publish("assessorflow.email.request.assessor-review", payload).get();
        log.info("[TEST] Published assessor-review email request for {}", event.getAssessorEmail());
        return ResponseEntity.ok(Map.of("status", "published", "topic", "assessorflow.email.request.assessor-review"));
    }

    @PostMapping("/assessment-link")
    public ResponseEntity<Map<String, String>> sendAssessmentLink(@RequestBody AssessmentLinkEvent event) throws Exception {
        String payload = objectMapper.writeValueAsString(event);
        pubSubTemplate.publish("assessorflow.email.request.assessment-link", payload).get();
        log.info("[TEST] Published assessment-link email request for {}", event.getParticipantEmail());
        return ResponseEntity.ok(Map.of("status", "published", "topic", "assessorflow.email.request.assessment-link"));
    }

    @PostMapping("/participant-report")
    public ResponseEntity<Map<String, String>> sendParticipantReport(@RequestBody ParticipantReportEvent event) throws Exception {
        String payload = objectMapper.writeValueAsString(event);
        pubSubTemplate.publish("assessorflow.email.request.participant-report", payload).get();
        log.info("[TEST] Published participant-report email request for {}", event.getParticipantEmail());
        return ResponseEntity.ok(Map.of("status", "published", "topic", "assessorflow.email.request.participant-report"));
    }
}