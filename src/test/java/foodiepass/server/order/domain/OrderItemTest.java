package foodiepass.server.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.price.domain.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    private MenuItem GUKBAP;

    @BeforeEach
    void setUp() {
        FoodInfo gukbapInfo = new FoodInfo("따끈한 국밥", "gukbap.jpg", "gukbap_preview.jpg");
        Price gukbapPrice = new Price(Currency.SOUTH_KOREAN_WON, 9000);
        GUKBAP = new MenuItem("든든한 국밥", gukbapPrice, gukbapInfo);
    }

    @Test
    @DisplayName("주문 항목의 총액을 정확히 계산한다")
    void calculateTotalPrice_shouldReturnCorrectPriceForQuantity() {
        // given
        int quantity = 3;
        OrderItem orderItem = new OrderItem(GUKBAP, quantity);
        Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, 27000);

        // when
        Price actualPrice = orderItem.calculateTotalPrice();

        // then
        assertThat(actualPrice).isEqualTo(expectedPrice);
    }

    @Test
    @DisplayName("주문 수량이 1일 때, 메뉴의 단가와 동일한 총액을 반환한다")
    void calculateTotalPrice_shouldReturnSamePriceForSingleQuantity() {
        // given
        int quantity = 1;
        OrderItem orderItem = new OrderItem(GUKBAP, quantity);

        // when
        Price actualPrice = orderItem.calculateTotalPrice();

        // then
        assertThat(actualPrice).isEqualTo(GUKBAP.getPrice());
    }

    @Test
    @DisplayName("주문 수량이 0일 때, 0원의 총액을 반환한다")
    void calculateTotalPrice_shouldReturnZeroPriceForZeroQuantity() {
        // given
        int quantity = 0;
        OrderItem orderItem = new OrderItem(GUKBAP, quantity);
        Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, 0);

        // when
        Price actualPrice = orderItem.calculateTotalPrice();

        // then
        assertThat(actualPrice).isEqualTo(expectedPrice);
    }
}
