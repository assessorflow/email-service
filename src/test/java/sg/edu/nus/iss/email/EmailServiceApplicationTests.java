package sg.edu.nus.iss.email;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestConfig.class)
class EmailServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
