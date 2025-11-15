package foodiepass.server.cache.domain;

import foodiepass.server.abtest.domain.MenuScan;
import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.domain.MenuItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing a cached menu item.
 * Stores menu item data to avoid redundant API calls for the same menu image.
 */
@Entity
@Table(name = "menu_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuItemEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_scan_id", nullable = false)
    private MenuScan menuScan;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAmount;

    @Column(nullable = false, length = 10)
    private String priceCurrency;

    private MenuItemEntity(MenuScan menuScan, String name, BigDecimal priceAmount,
                          String priceCurrency) {
        this.id = UUID.randomUUID();
        this.menuScan = menuScan;
        this.name = name;
        this.priceAmount = priceAmount;
        this.priceCurrency = priceCurrency;
    }

    /**
     * Factory method to create MenuItemEntity from MenuItem (OCR result) and MenuScan.
     * Note: MenuItem from OCR has null FoodInfo, which is populated during enrichment.
     */
    public static MenuItemEntity from(MenuItem menuItem, MenuScan menuScan) {
        return new MenuItemEntity(
                menuScan,
                menuItem.getName(),
                menuItem.getPrice().getAmount(),
                menuItem.getPrice().getCurrency().getCurrencyCode()
        );
    }

    /**
     * Convert this entity to MenuItem domain object (OCR result format).
     * Returns MenuItem with null FoodInfo, which will be enriched later.
     */
    public MenuItem toMenuItem() {
        Currency currency = Currency.fromCurrencyCode(this.priceCurrency);
        Price price = new Price(currency, this.priceAmount);
        return new MenuItem(this.name, price, null);
    }
}
