package foodiepass.server.menu.infra.scraper.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MenuItemOcrDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private double price;

    public MenuItemOcrDto(String name, double price) {
        this.name = name;
        this.price = price;
    }
}
