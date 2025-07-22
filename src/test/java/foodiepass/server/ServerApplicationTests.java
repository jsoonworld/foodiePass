package foodiepass.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // import 추가

@ActiveProfiles("test") // "test" 프로파일을 활성화하여 application-test.yml을 로드합니다.
@SpringBootTest
class ServerApplicationTests {

	@Test
	void contextLoads() {
		// 이제 application-test.yml의 모든 설정값을 사용하여
		// 애플리케이션 컨텍스트가 성공적으로 로드됩니다.
	}

}
