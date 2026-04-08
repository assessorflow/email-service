package sg.edu.nus.iss.email.dto.assessor_review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessorReviewEvent implements Serializable {

    // Fields from pubsub.md §5.25
    private String assessmentId;
    private String assessorEmail;
    private String reviewLink;
    private Integer questionCount;

    // Extra fields for template rendering (Orchestrator includes these)
    private String workflowId;
    private String assessorName;
    private String assessmentName;
}
