package sg.edu.nus.iss.email.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {

    private final SesV2Client sesV2Client;
    private final SpringTemplateEngine templateEngine;


    
    @Value("${aws.ses.from-email}")
    private String fromEmail;

    public String renderTemplate(String templateName, Map<String, Object> templateData) {
        Context context = new Context();
        if (templateData != null) {
            context.setVariables(templateData);
        }
        return templateEngine.process(templateName, context);
    }

    public void sendRenderedHtml(String to, String subject, String htmlBody) {
        SendEmailRequest request = SendEmailRequest.builder()
                .fromEmailAddress(fromEmail)
                .destination(Destination.builder().toAddresses(to).build())
                .content(EmailContent.builder()
                        .simple(Message.builder()
                                .subject(Content.builder().data(subject).build())
                                .body(Body.builder()
                                        .html(Content.builder().data(htmlBody).build())
                                        .build())
                                .build())
                        .build())
                .build();

        SendEmailResponse response = sesV2Client.sendEmail(request);
        log.info("Email sent via SES to {}, messageId={}", to, response.messageId());
    }
}
