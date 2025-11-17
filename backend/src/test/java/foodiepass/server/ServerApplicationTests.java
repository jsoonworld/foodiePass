package foodiepass.server;

import foodiepass.server.config.MockExternalDependenciesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
@Import(MockExternalDependenciesConfig.class)
class ServerApplicationTests {

	@Test
	void contextLoads() {
	}
}
