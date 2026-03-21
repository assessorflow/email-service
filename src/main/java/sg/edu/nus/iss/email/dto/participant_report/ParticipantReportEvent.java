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

    private String workflowId;
    private String recipientEmail;
    private String participantName;
    private String assessmentName;
    private String score;
    private String reportLink;
    private String subject;
    private String emailLogId;
}
