package foodiepass.server.language.api;

import foodiepass.server.global.success.SuccessResponse;
import foodiepass.server.language.domain.Language;
import foodiepass.server.language.dto.response.LanguageResponse;
import foodiepass.server.menu.application.port.out.FoodScrapper;
import foodiepass.server.menu.application.port.out.OcrReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("LanguageController 통합 테스트 - 실제 API 호출")
class LanguageControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public OcrReader ocrReader() {
            return mock(OcrReader.class);
        }

        @Bean
        public FoodScrapper foodScrapper() {
            return mock(FoodScrapper.class);
        }
    }

    @Test
    @DisplayName("GET /language - 모든 언어 목록을 반환한다")
    void getLanguages_returnsAllLanguages() {
        // when: 실제 HTTP GET 요청
        ResponseEntity<SuccessResponse<List<LanguageResponse>>> response = restTemplate.exchange(
                "/language",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then: 상태 코드 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // then: SuccessResponse 검증
        SuccessResponse<List<LanguageResponse>> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(200);
        assertThat(body.message()).isNotBlank();

        // then: 응답 result가 존재하고 비어있지 않음
        List<LanguageResponse> languages = body.result();
        assertThat(languages).isNotNull();
        assertThat(languages).isNotEmpty();

        // then: 모든 Language enum 값이 포함되어야 함
        assertThat(languages).hasSizeGreaterThanOrEqualTo(Language.values().length);
    }

    @Test
    @DisplayName("GET /language - 각 언어 응답에 languageName이 포함된다")
    void getLanguages_eachLanguageContainsLanguageName() {
        // when: 실제 HTTP GET 요청
        ResponseEntity<SuccessResponse<List<LanguageResponse>>> response = restTemplate.exchange(
                "/language",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then: 각 언어가 languageName을 가져야 함
        SuccessResponse<List<LanguageResponse>> body = response.getBody();
        assertThat(body).isNotNull();

        List<LanguageResponse> languages = body.result();
        assertThat(languages).isNotNull();

        languages.forEach(language -> {
            assertThat(language.languageName()).isNotBlank();
        });
    }

    @Test
    @DisplayName("GET /language - 특정 언어들이 응답에 포함된다")
    void getLanguages_containsExpectedLanguages() {
        // when: 실제 HTTP GET 요청
        ResponseEntity<SuccessResponse<List<LanguageResponse>>> response = restTemplate.exchange(
                "/language",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then: 예상되는 언어 이름들이 포함되어야 함
        SuccessResponse<List<LanguageResponse>> body = response.getBody();
        assertThat(body).isNotNull();

        List<LanguageResponse> languages = body.result();
        assertThat(languages).isNotNull();

        List<String> languageNames = languages.stream()
                .map(LanguageResponse::languageName)
                .toList();

        // 최소한 이 언어들은 포함되어야 함
        List<String> expectedLanguageNames = Arrays.stream(Language.values())
                .map(Language::getLanguageName)
                .toList();

        assertThat(languageNames).containsAll(expectedLanguageNames);
    }

    @Test
    @DisplayName("GET /language - 여러 번 호출해도 동일한 결과를 반환한다 (캐싱 검증)")
    void getLanguages_multipleCallsReturnSameResult() {
        // when: 동일한 요청을 2번 수행
        ResponseEntity<SuccessResponse<List<LanguageResponse>>> firstResponse = restTemplate.exchange(
                "/language",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        ResponseEntity<SuccessResponse<List<LanguageResponse>>> secondResponse = restTemplate.exchange(
                "/language",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        // then: 두 응답이 동일해야 함
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<LanguageResponse> firstLanguages = firstResponse.getBody().result();
        List<LanguageResponse> secondLanguages = secondResponse.getBody().result();

        assertThat(firstLanguages)
                .isNotNull()
                .hasSize(secondLanguages.size())
                .containsExactlyElementsOf(secondLanguages);
    }
}
