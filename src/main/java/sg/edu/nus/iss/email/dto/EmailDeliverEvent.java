package sg.edu.nus.iss.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Deliver stage payload (pubsub.md §5.28).
 * Contains pre-rendered HTML — the deliver stage only sends, no template rendering.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDeliverEvent implements Serializable {

    private String emailLogId;
    private String recipientEmail;
    private String subject;
    private String templateId;
    private String renderedHtml;
}
