package sg.edu.nus.iss.email.dto.assessor_review;

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
public class AssessorReviewEvent {

    @NotBlank
    @JsonAlias({"assessment_id", "assessmentId"})
    private String assessmentId;

    @NotBlank @Email
    @JsonAlias({"assessor_email", "assessorEmail"})
    private String assessorEmail;

    @NotBlank
    @JsonAlias({"review_link", "reviewLink"})
    private String reviewLink;

    @JsonAlias({"question_count", "questionCount"})
    private Integer questionCount;

    @NotBlank
    @JsonAlias({"workflow_id", "workflowId"})
    private String workflowId;

    @NotBlank
    @JsonAlias({"assessor_name", "assessorName"})
    private String assessorName;

    @NotBlank
    @JsonAlias({"assessment_name", "assessmentName"})
    private String assessmentName;
}
