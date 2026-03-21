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

    // ── Assessor Review: Request ──────────────────────────
    @Bean
    public MessageChannel assessorReviewRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter assessorReviewRequestAdapter(
            PubSubTemplate pubSubTemplate,
            @Qualifier("assessorReviewRequestChannel") MessageChannel channel,
            @Value("${app.pubsub.assessor-review.request-subscription}") String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    // ── Assessor Review: Deliver ──────────────────────────
    @Bean
    public MessageChannel assessorReviewDeliverChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter assessorReviewDeliverAdapter(
            PubSubTemplate pubSubTemplate,
            @Qualifier("assessorReviewDeliverChannel") MessageChannel channel,
            @Value("${app.pubsub.assessor-review.deliver-subscription}") String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    // ── Assessment Link: Request ──────────────────────────
    @Bean
    public MessageChannel assessmentLinkRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter assessmentLinkRequestAdapter(
            PubSubTemplate pubSubTemplate,
            @Qualifier("assessmentLinkRequestChannel") MessageChannel channel,
            @Value("${app.pubsub.assessment-link.request-subscription}") String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    // ── Assessment Link: Deliver ──────────────────────────
    @Bean
    public MessageChannel assessmentLinkDeliverChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter assessmentLinkDeliverAdapter(
            PubSubTemplate pubSubTemplate,
            @Qualifier("assessmentLinkDeliverChannel") MessageChannel channel,
            @Value("${app.pubsub.assessment-link.deliver-subscription}") String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    // ── Participant Report: Request ──────────────────────────
    @Bean
    public MessageChannel participantReportRequestChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter participantReportRequestAdapter(
            PubSubTemplate pubSubTemplate,
            @Qualifier("participantReportRequestChannel") MessageChannel channel,
            @Value("${app.pubsub.participant-report.request-subscription}") String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    // ── Participant Report: Deliver ──────────────────────────
    @Bean
    public MessageChannel participantReportDeliverChannel() {
        return new DirectChannel();
    }

    @Bean
    public PubSubInboundChannelAdapter participantReportDeliverAdapter(
            PubSubTemplate pubSubTemplate,
            @Qualifier("participantReportDeliverChannel") MessageChannel channel,
            @Value("${app.pubsub.participant-report.deliver-subscription}") String subscription) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscription);
        adapter.setOutputChannel(channel);
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }
}
