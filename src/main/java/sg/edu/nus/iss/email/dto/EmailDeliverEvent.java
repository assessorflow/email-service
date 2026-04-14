package sg.edu.nus.iss.email.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Deliver stage payload (pubsub.md §5.28).
 * Contains pre-rendered HTML — the deliver stage only sends, no template rendering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDeliverEvent {

    @JsonAlias({"email_log_id", "emailLogId"})
    private String emailLogId;

    @JsonAlias({"recipient_email", "recipientEmail"})
    private String recipientEmail;

    private String subject;

    @JsonAlias({"rendered_html", "renderedHtml"})
    private String renderedHtml;
}
