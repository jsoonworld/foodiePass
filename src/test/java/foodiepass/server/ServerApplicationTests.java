package foodiepass.server;

import foodiepass.server.menu.application.port.out.OcrReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("test")
@SpringBootTest
class ServerApplicationTests {

	@MockitoBean
	private OcrReader ocrReader;

	@Test
	void contextLoads() {
	}
}
