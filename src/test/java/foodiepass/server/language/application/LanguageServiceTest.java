package foodiepass.server.language.application;

import foodiepass.server.language.domain.Language;
import foodiepass.server.language.dto.response.LanguageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LanguageServiceTest {

    private LanguageService languageService;

    @BeforeEach
    void setUp() {
        languageService = new LanguageService();
    }

    @Test
    @DisplayName("findAllLanguages 호출 시 전체 언어 목록을 DTO로 변환하여 반환한다")
    void findAllLanguages_shouldReturnCorrectLanguageList() {
        // given
        int expectedSize = Language.values().length;

        // when
        List<LanguageResponse> actualLanguages = languageService.findAllLanguages();

        // then
        assertThat(actualLanguages).isNotNull();
        assertThat(actualLanguages).hasSize(expectedSize);

        // Language Enum의 모든 languageName이 DTO 리스트에 포함되어 있는지 확인
        List<String> expectedNames = Arrays.stream(Language.values())
                .map(Language::getLanguageName)
                .toList();

        assertThat(actualLanguages)
                .extracting(LanguageResponse::languageName)
                .containsExactlyInAnyOrderElementsOf(expectedNames);
    }

    @Test
    @DisplayName("findAllLanguages가 반환한 리스트는 수정할 수 없다")
    void findAllLanguages_shouldReturnUnmodifiableList() {
        // given
        List<LanguageResponse> actualLanguages = languageService.findAllLanguages();
        LanguageResponse newLanguage = new LanguageResponse("Klingon"); // 임의의 새 언어

        // when & then
        // 리스트에 요소를 추가하려고 할 때 UnsupportedOperationException이 발생하는지 검증
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> actualLanguages.add(newLanguage));

        // 리스트의 요소를 제거하려고 할 때도 동일하게 예외가 발생하는지 검증
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> actualLanguages.remove(0));
    }

    @Test
    @DisplayName("findAllLanguages를 여러 번 호출해도 항상 동일한 인스턴스를 반환한다")
    void findAllLanguages_shouldReturnSameInstanceOnMultipleCalls() {
        // given & when
        List<LanguageResponse> result1 = languageService.findAllLanguages();
        List<LanguageResponse> result2 = languageService.findAllLanguages();

        // then
        // 두 결과가 내용만 같은 것(equals)이 아니라, 완전히 동일한 객체(인스턴스)인지 검증
        assertThat(result1).isSameAs(result2);
    }
}
