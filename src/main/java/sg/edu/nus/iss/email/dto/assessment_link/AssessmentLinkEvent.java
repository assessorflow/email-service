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

    private String workflowId;
    private String recipientEmail;
    private String participantName;
    private String assessmentName;
    private String assessmentLink;
    private String subject;
    private String emailLogId;
}
