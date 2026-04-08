package sg.edu.nus.iss.email.dto.assessment_link;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentLinkEvent implements Serializable {

    // Fields from pubsub.md §5.26
    private String assessmentId;
    private String participantEmail;
    private String assessmentLink;
    private Integer durationMinutes;
    private String deadline;

    // Extra fields for template rendering (Orchestrator includes these)
    private String workflowId;
    private String participantName;
    private String assessmentName;
}
