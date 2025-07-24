package foodiepass.server.menu.domain;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.exception.FoodErrorCode;
import foodiepass.server.menu.exception.FoodException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MenuItem 클래스")
class MenuItemTest {

    private Price validPrice;
    private FoodInfo validFoodInfo;

    @BeforeEach
    void setUp() {
        validPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("8000"));
        validFoodInfo = new FoodInfo("김치찌개", "매콤한 김치찌개", "kimchi.jpg", "kimchi_preview.jpg");
    }

    @Nested
    @DisplayName("생성자는")
    class Describe_constructor {

        @Test
        @DisplayName("유효한 정보로 객체를 성공적으로 생성한다")
        void shouldCreateMenuItemSuccessfully() {
            // when
            MenuItem menuItem = new MenuItem("돼지고기 김치찌개", validPrice, validFoodInfo);

            // then
            assertThat(menuItem.getName()).isEqualTo("돼지고기 김치찌개");
            assertThat(menuItem.getPrice()).isEqualTo(validPrice);
            assertThat(menuItem.getFoodInfo()).isEqualTo(validFoodInfo);
        }

        @DisplayName("메뉴 이름이 유효하지 않으면 FoodException을 던진다")
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t"})
        void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
            // when & then
            assertThatThrownBy(() -> new MenuItem(invalidName, validPrice, validFoodInfo))
                    .isInstanceOf(FoodException.class)
                    .hasMessage(FoodErrorCode.INVALID_MENU_ITEM_NAME.getMessage());
        }

        @Test
        @DisplayName("가격 정보가 null이면 FoodException을 던진다")
        void shouldThrowExceptionWhenPriceIsNull() {
            // when & then
            assertThatThrownBy(() -> new MenuItem("정상 메뉴 이름", null, validFoodInfo))
                    .isInstanceOf(FoodException.class)
                    .hasMessage(FoodErrorCode.INVALID_MENU_ITEM_PRICE.getMessage());
        }
    }
}
