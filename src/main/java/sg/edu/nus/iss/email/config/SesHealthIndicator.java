package sg.edu.nus.iss.email.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.GetAccountRequest;

@Component
@RequiredArgsConstructor
public class SesHealthIndicator extends AbstractHealthIndicator {

    private final SesV2Client sesV2Client;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            sesV2Client.getAccount(GetAccountRequest.builder().build());
            builder.up().withDetail("ses", "connected");
        } catch (Exception e) {
            builder.down(e).withDetail("ses", "unreachable");
        }
    }
}
