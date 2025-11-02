package foodiepass.server.menu.domain;

import foodiepass.server.menu.exception.FoodErrorCode;
import foodiepass.server.menu.exception.FoodException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FoodInfo 클래스")
class FoodInfoTest {

    @Nested
    @DisplayName("생성자는")
    class Describe_constructor {

        @Test
        @DisplayName("유효한 정보로 객체를 성공적으로 생성한다")
        void shouldCreateFoodInfoSuccessfully() {
            // given
            String name = "김치찌개";
            String description = "매콤한 김치찌개";
            String image = "kimchi.jpg";
            String previewImage = "kimchi_preview.jpg";

            // when
            FoodInfo foodInfo = new FoodInfo(name, description, image, previewImage);

            // then
            assertThat(foodInfo.getName()).isEqualTo(name);
            assertThat(foodInfo.getDescription()).isEqualTo(description);
            assertThat(foodInfo.getImage()).isEqualTo(image);
            assertThat(foodInfo.getPreviewImage()).isEqualTo(previewImage);
        }

        @DisplayName("음식 이름이 유효하지 않으면 FoodException을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t"})
        void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
            // when & then
            assertThatThrownBy(() -> new FoodInfo(invalidName, "description", "image", "preview"))
                    .isInstanceOf(FoodException.class)
                    .hasMessage(FoodErrorCode.INVALID_FOOD_NAME.getMessage());
        }
    }
}
