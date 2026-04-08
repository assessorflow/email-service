package sg.edu.nus.iss.email.dto.participant_report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantReportEvent implements Serializable {

    // Fields from pubsub.md §5.27
    private String assessmentId;
    private String participantEmail;
    private String reportId;
    private String reportLink;

    // Extra fields for template rendering (Orchestrator includes these)
    private String workflowId;
    private String participantName;
    private String assessmentName;
    private String score;
}
