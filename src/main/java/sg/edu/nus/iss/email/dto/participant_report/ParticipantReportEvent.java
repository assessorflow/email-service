package sg.edu.nus.iss.email.dto.participant_report;

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
public class ParticipantReportEvent {

    @NotBlank
    @JsonAlias({"assessment_id", "assessmentId"})
    private String assessmentId;

    @NotBlank @Email
    @JsonAlias({"participant_email", "participantEmail"})
    private String participantEmail;

    @NotBlank
    @JsonAlias({"report_id", "reportId"})
    private String reportId;

    @NotBlank
    @JsonAlias({"report_link", "reportLink"})
    private String reportLink;

    @NotBlank
    @JsonAlias({"workflow_id", "workflowId"})
    private String workflowId;

    @NotBlank
    @JsonAlias({"participant_name", "participantName"})
    private String participantName;

    @NotBlank
    @JsonAlias({"assessment_name", "assessmentName"})
    private String assessmentName;

    private String score;
}
