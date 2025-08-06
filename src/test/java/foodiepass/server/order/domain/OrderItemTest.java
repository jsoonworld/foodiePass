package foodiepass.server.order.domain;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.order.exception.OrderErrorCode;
import foodiepass.server.order.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderItem 클래스")
class OrderItemTest {

    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        FoodInfo gukbapInfo = new FoodInfo("든든한 국밥", "따끈하고 든든한 국밥입니다.", "gukbap.jpg", "gukbap_preview.jpg");
        Price gukbapPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("9000"));
        menuItem = new MenuItem("든든한 국밥", gukbapPrice, gukbapInfo);
    }

    @Nested
    @DisplayName("생성자는")
    class Describe_constructor {

        @Test
        @DisplayName("올바른 메뉴 항목과 수량으로 OrderItem 객체를 성공적으로 생성한다")
        void shouldCreateOrderItemSuccessfully() {
            // when
            OrderItem orderItem = new OrderItem(menuItem, 1);

            // then
            assertThat(orderItem.getMenuItem()).isEqualTo(menuItem);
            assertThat(orderItem.getQuantity()).isEqualTo(1);
        }

        @Test
        @DisplayName("메뉴 항목이 null이면 OrderException(NULL_MENU_ITEM)을 던진다")
        void shouldThrowExceptionWhenMenuItemIsNull() {
            // when & then
            assertThatThrownBy(() -> new OrderItem(null, 1))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(OrderErrorCode.NULL_MENU_ITEM.getMessage());
        }

        @DisplayName("주문 수량이 0 이하이면 OrderException(INVALID_ORDER_QUANTITY)을 던진다")
        @ParameterizedTest
        @ValueSource(ints = {0, -1, -100})
        void shouldThrowExceptionWhenQuantityIsZeroOrNegative(int invalidQuantity) {
            // when & then
            assertThatThrownBy(() -> new OrderItem(menuItem, invalidQuantity))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(OrderErrorCode.INVALID_ORDER_QUANTITY.getMessage());
        }
    }

    @Nested
    @DisplayName("calculateTotalPrice 메서드는")
    class Describe_calculateTotalPrice {

        @Test
        @DisplayName("주문 항목의 총액(단가 * 수량)을 정확히 계산한다")
        void shouldReturnCorrectPriceForQuantity() {
            // given
            int quantity = 3;
            OrderItem orderItem = new OrderItem(menuItem, quantity);
            Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("27000"));

            // when
            Price actualPrice = orderItem.calculateTotalPrice();

            // then
            assertThat(actualPrice).isEqualTo(expectedPrice);
        }
    }
}
