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

    private String workflowId;
    private String recipientEmail;
    private String assessorName;
    private String assessmentName;
    private String reviewLink;
    private String subject;
    private String emailLogId;
}
