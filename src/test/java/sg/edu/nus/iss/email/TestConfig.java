package sg.edu.nus.iss.email;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

/**
 * Provides mock PubSubTemplate for tests — GCP Pub/Sub is not available in test profile.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public PubSubTemplate pubSubTemplate() {
        return mock(PubSubTemplate.class);
    }
}
