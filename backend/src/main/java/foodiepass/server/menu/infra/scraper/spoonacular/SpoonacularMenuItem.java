package foodiepass.server.menu.infra.scraper.spoonacular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpoonacularMenuItem(
        Long id,
        String title,
        String image,
        String imageType,
        String restaurantChain
) {}
