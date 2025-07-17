package foodiepass.server.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import foodiepass.server.currency.domain.Currency;
import foodiepass.server.price.domain.Price;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OrderTest {

    private MenuItem GUKBAP;
    private MenuItem KIMCHI_JJIGAE;

    @BeforeEach
    void setUp() {
        FoodInfo gukbapInfo = new FoodInfo("따끈한 국밥", "gukbap.jpg", "gukbap_preview.jpg");
        Price gukbapPrice = new Price(Currency.SOUTH_KOREAN_WON, 9000);
        GUKBAP = new MenuItem("든든한 국밥", gukbapPrice, gukbapInfo);

        FoodInfo kimchiInfo = new FoodInfo("매콤한 김치찌개", "kimchi.jpg", "kimchi_preview.jpg");
        Price kimchiPrice = new Price(Currency.SOUTH_KOREAN_WON, 8000);
        KIMCHI_JJIGAE = new MenuItem("돼지고기 김치찌개", kimchiPrice, kimchiInfo);
    }

    @Test
    @DisplayName("여러 주문 항목들의 총액을 정확히 계산한다")
    void calculateTotalPrice_shouldSumUpAllOrderItems() {
        // given
        OrderItem gukbapOrder = new OrderItem(GUKBAP, 2);
        OrderItem kimchiOrder = new OrderItem(KIMCHI_JJIGAE, 1);
        List<OrderItem> items = List.of(gukbapOrder, kimchiOrder);

        Order order = new Order(items, Currency.SOUTH_KOREAN_WON);

        Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, 26000);

        // when
        Price actualPrice = order.calculateTotalPrice();

        // then
        assertThat(actualPrice).isEqualTo(expectedPrice);
    }

    @Test
    @DisplayName("주문 항목이 하나일 때, 해당 항목의 총액을 반환한다")
    void calculateTotalPrice_shouldWorkForSingleItem() {
        // given
        OrderItem gukbapOrder = new OrderItem(GUKBAP, 3);
        List<OrderItem> items = List.of(gukbapOrder);

        Order order = new Order(items, Currency.SOUTH_KOREAN_WON);

        Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, 27000);

        // when
        Price actualPrice = order.calculateTotalPrice();

        // then
        assertThat(actualPrice).isEqualTo(expectedPrice);
    }

    @Test
    @DisplayName("주문 항목이 없을 때, 0원의 총액을 반환한다")
    void calculateTotalPrice_shouldReturnZeroForEmptyOrder() {
        // given
        List<OrderItem> emptyItems = Collections.emptyList();
        Order order = new Order(emptyItems, Currency.SOUTH_KOREAN_WON);

        Price expectedPrice = new Price(Currency.SOUTH_KOREAN_WON, 0);

        // when
        Price actualPrice = order.calculateTotalPrice();

        // then
        assertThat(actualPrice).isEqualTo(expectedPrice);
    }
}
