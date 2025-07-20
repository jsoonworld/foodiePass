package foodiepass.server.order.domain;

import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.food.domain.FoodInfo;
import foodiepass.server.food.domain.MenuItem;
import foodiepass.server.order.exception.OrderErrorCode;
import foodiepass.server.order.exception.OrderException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order 클래스")
class OrderTest {

    private MenuItem gukbap;
    private MenuItem kimchiJjigae;
    private MenuItem steak;

    @BeforeEach
    void setUp() {
        gukbap = new MenuItem(
                "든든한 국밥",
                new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("9000")),
                new FoodInfo("든든한 국밥", "따끈하고 든든한 국밥입니다.", "gukbap.jpg", "gukbap_preview.jpg")
        );

        kimchiJjigae = new MenuItem(
                "돼지고기 김치찌개",
                new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("8000")),
                new FoodInfo("돼지고기 김치찌개", "매콤하고 칼칼한 김치찌개입니다.", "kimchi.jpg", "kimchi_preview.jpg")
        );

        steak = new MenuItem(
                "T-Bone Steak",
                new Price(Currency.UNITED_STATES_DOLLAR, new BigDecimal("55.50")),
                new FoodInfo("T-Bone Steak", "A juicy T-bone steak.", "steak.jpg", "steak_preview.jpg")
        );
    }

    @Nested
    @DisplayName("생성자는")
    class Describe_constructor {

        @Test
        @DisplayName("올바른 주문 항목과 통화로 Order 객체를 성공적으로 생성한다")
        void shouldCreateOrderSuccessfully() {
            // given
            OrderItem gukbapOrder = new OrderItem(gukbap, 1);
            List<OrderItem> items = List.of(gukbapOrder);

            // when
            Order order = new Order(items, Currency.SOUTH_KOREAN_WON);

            // then
            assertThat(order.getItems()).isEqualTo(items);
            assertThat(order.getCurrency()).isEqualTo(Currency.SOUTH_KOREAN_WON);
        }

        @Test
        @DisplayName("주문 항목 리스트가 null이면 OrderException을 던진다")
        void shouldThrowExceptionWhenItemsAreNull() {
            // when & then
            assertThatThrownBy(() -> new Order(null, Currency.SOUTH_KOREAN_WON))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(OrderErrorCode.EMPTY_ORDER_ITEMS.getMessage());
        }

        @Test
        @DisplayName("주문 항목 리스트가 비어있으면 OrderException을 던진다")
        void shouldThrowExceptionWhenItemsAreEmpty() {
            // given
            List<OrderItem> emptyItems = Collections.emptyList();

            // when & then
            assertThatThrownBy(() -> new Order(emptyItems, Currency.SOUTH_KOREAN_WON))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(OrderErrorCode.EMPTY_ORDER_ITEMS.getMessage());
        }

        @Test
        @DisplayName("통화 정보가 null이면 OrderException을 던진다")
        void shouldThrowExceptionWhenCurrencyIsNull() {
            // given
            OrderItem gukbapOrder = new OrderItem(gukbap, 1);
            List<OrderItem> items = List.of(gukbapOrder);

            // when & then
            assertThatThrownBy(() -> new Order(items, null))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(OrderErrorCode.NULL_CURRENCY.getMessage());
        }

        @Test
        @DisplayName("주문 통화와 다른 통화의 주문 항목이 있으면 OrderException을 던진다")
        void shouldThrowExceptionForMismatchedCurrencies() {
            // given
            OrderItem gukbapOrder = new OrderItem(gukbap, 1);
            OrderItem steakOrder = new OrderItem(steak, 1);
            List<OrderItem> mixedCurrencyItems = List.of(gukbapOrder, steakOrder);

            // when & then
            assertThatThrownBy(() -> new Order(mixedCurrencyItems, Currency.SOUTH_KOREAN_WON))
                    .isInstanceOf(OrderException.class)
                    .hasMessage(OrderErrorCode.DIFFERENT_CURRENCIES_IN_ORDER.getMessage());
        }
    }


    @Nested
    @DisplayName("calculateTotalPrice 메서드는")
    class Describe_calculateTotalPrice {

        @Test
        @DisplayName("여러 주문 항목들의 총액을 정확히 계산한다")
        void shouldSumUpAllOrderItems() {
            // given
            OrderItem gukbapOrder = new OrderItem(gukbap, 2);
            OrderItem kimchiOrder = new OrderItem(kimchiJjigae, 1);
            Order order = new Order(List.of(gukbapOrder, kimchiOrder), Currency.SOUTH_KOREAN_WON);

            Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("26000"));

            // when
            Price actualPrice = order.calculateTotalPrice();

            // then
            assertThat(actualPrice).isEqualTo(expectedPrice);
        }

        @Test
        @DisplayName("주문 항목이 하나일 때, 해당 항목의 총액을 반환한다")
        void shouldWorkForSingleItem() {
            // given
            OrderItem gukbapOrder = new OrderItem(gukbap, 3); // 27000
            Order order = new Order(List.of(gukbapOrder), Currency.SOUTH_KOREAN_WON);

            Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, new BigDecimal("27000"));

            // when
            Price actualPrice = order.calculateTotalPrice();

            // then
            assertThat(actualPrice).isEqualTo(expectedPrice);
        }
    }
}
