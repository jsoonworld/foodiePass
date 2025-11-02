package foodiepass.server.menu.infra.scraper.gemini;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class GeminiResponseParserTest {

    private final GeminiResponseParser parser = new GeminiResponseParser();

    @DisplayName("응답이 마크다운 JSON 코드 블록으로 감싸져 있을 때 순수 JSON만 추출한다")
    @ParameterizedTest
    @ValueSource(strings = {
            "```json\n{\"key\": \"value\"}\n```",
            "```\n{\"key\": \"value\"}\n```",
            "some text before ```json\n{\"key\": \"value\"}\n``` some text after"
    })
    void parse_whenResponseIsMarkdownJson_extractsJson(final String rawResponse) {
        // given
        final String expectedJson = "{\"key\": \"value\"}";

        // when
        final String result = parser.parse(rawResponse);

        // then
        assertThat(result).isEqualTo(expectedJson);
    }

    @Test
    @DisplayName("응답이 순수 JSON 문자열일 때 그대로 반환한다")
    void parse_whenResponseIsPureJson_returnsAsIs() {
        // given
        final String rawJson = "{\"key\": \"value\"}";

        // when
        final String result = parser.parse(rawJson);

        // then
        assertThat(result).isEqualTo(rawJson);
    }

    @Test
    @DisplayName("응답에 Java Escape 문자가 포함된 경우 Unescape 처리한다")
    void parse_whenResponseHasEscapedChars_unescapesIt() {
        // given
        final String rawResponseWithEscapes = """
            ```json
            {\\"key\\": \\"value\\"}
            ```
            """;
        final String expectedJson = "{\"key\": \"value\"}";

        // when
        final String result = parser.parse(rawResponseWithEscapes);

        // then
        assertThat(result).isEqualTo(expectedJson);
    }
}
