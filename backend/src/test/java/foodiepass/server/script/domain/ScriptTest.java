package foodiepass.server.script.domain;

import foodiepass.server.script.exception.ScriptErrorCode;
import foodiepass.server.script.exception.ScriptException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Script 클래스")
class ScriptTest {

    @Nested
    @DisplayName("생성자는")
    class Describe_constructor {

        @Test
        @DisplayName("유효한 스크립트로 객체를 성공적으로 생성한다")
        void shouldCreateScriptSuccessfully() {
            // given
            String travelerScript = "안녕하세요";
            String localScript = "Hello";

            // when
            Script script = new Script(travelerScript, localScript);

            // then
            assertThat(script.getTravelerScript()).isEqualTo(travelerScript);
            assertThat(script.getLocalScript()).isEqualTo(localScript);
        }

        @DisplayName("여행자 스크립트가 유효하지 않으면 ScriptException(INVALID_TRAVELER_SCRIPT)을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldThrowExceptionWhenTravelerScriptIsInvalid(String invalidScript) {
            // when & then
            assertThatThrownBy(() -> new Script(invalidScript, "Valid Script"))
                    .isInstanceOf(ScriptException.class)
                    .hasMessage(ScriptErrorCode.INVALID_TRAVELER_SCRIPT.getMessage());
        }

        @DisplayName("현지인 스크립트가 유효하지 않으면 ScriptException(INVALID_LOCAL_SCRIPT)을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "   ", "\t", "\n"})
        void shouldThrowExceptionWhenLocalScriptIsInvalid(String invalidScript) {
            // when & then
            assertThatThrownBy(() -> new Script("Valid Script", invalidScript))
                    .isInstanceOf(ScriptException.class)
                    .hasMessage(ScriptErrorCode.INVALID_LOCAL_SCRIPT.getMessage());
        }
    }
}
