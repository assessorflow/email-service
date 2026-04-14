package sg.edu.nus.iss.email.config;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.integration.AckMode;
import com.google.cloud.spring.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class PubSubConfig {

    private PubSubInboundChannelAdapter createAdapter(PubSubTemplate template, MessageChannel channel, String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(template, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    @Bean public MessageChannel assessorReviewRequestChannel()    { return new DirectChannel(); }
    @Bean public MessageChannel assessorReviewDeliverChannel()    { return new DirectChannel(); }
    @Bean public MessageChannel assessmentLinkRequestChannel()    { return new DirectChannel(); }
    @Bean public MessageChannel assessmentLinkDeliverChannel()    { return new DirectChannel(); }
    @Bean public MessageChannel participantReportRequestChannel() { return new DirectChannel(); }
    @Bean public MessageChannel participantReportDeliverChannel() { return new DirectChannel(); }

    @Bean
    public PubSubInboundChannelAdapter assessorReviewRequestAdapter(
            PubSubTemplate t, @Qualifier("assessorReviewRequestChannel") MessageChannel ch,
            @Value("${app.pubsub.assessor-review.request-subscription}") String sub) {
        return createAdapter(t, ch, sub);
    }

    @Bean
    public PubSubInboundChannelAdapter assessorReviewDeliverAdapter(
            PubSubTemplate t, @Qualifier("assessorReviewDeliverChannel") MessageChannel ch,
            @Value("${app.pubsub.assessor-review.deliver-subscription}") String sub) {
        return createAdapter(t, ch, sub);
    }

    @Bean
    public PubSubInboundChannelAdapter assessmentLinkRequestAdapter(
            PubSubTemplate t, @Qualifier("assessmentLinkRequestChannel") MessageChannel ch,
            @Value("${app.pubsub.assessment-link.request-subscription}") String sub) {
        return createAdapter(t, ch, sub);
    }

    @Bean
    public PubSubInboundChannelAdapter assessmentLinkDeliverAdapter(
            PubSubTemplate t, @Qualifier("assessmentLinkDeliverChannel") MessageChannel ch,
            @Value("${app.pubsub.assessment-link.deliver-subscription}") String sub) {
        return createAdapter(t, ch, sub);
    }

    @Bean
    public PubSubInboundChannelAdapter participantReportRequestAdapter(
            PubSubTemplate t, @Qualifier("participantReportRequestChannel") MessageChannel ch,
            @Value("${app.pubsub.participant-report.request-subscription}") String sub) {
        return createAdapter(t, ch, sub);
    }

    @Bean
    public PubSubInboundChannelAdapter participantReportDeliverAdapter(
            PubSubTemplate t, @Qualifier("participantReportDeliverChannel") MessageChannel ch,
            @Value("${app.pubsub.participant-report.deliver-subscription}") String sub) {
        return createAdapter(t, ch, sub);
    }
}