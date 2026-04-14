package sg.edu.nus.iss.email.dto.assessment_link;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentLinkEvent {

    @NotBlank
    @JsonAlias({"assessment_id", "assessmentId"})
    private String assessmentId;

    @NotBlank @Email
    @JsonAlias({"participant_email", "participantEmail"})
    private String participantEmail;

    @NotBlank
    @JsonAlias({"assessment_link", "assessmentLink"})
    private String assessmentLink;

    @JsonAlias({"duration_minutes", "durationMinutes"})
    private Integer durationMinutes;

    private String deadline;

    @NotBlank
    @JsonAlias({"workflow_id", "workflowId"})
    private String workflowId;

    @NotBlank
    @JsonAlias({"participant_name", "participantName"})
    private String participantName;

    @NotBlank
    @JsonAlias({"assessment_name", "assessmentName"})
    private String assessmentName;
}
