package foodiepass.server.menu.infra.scraper.spoonacular;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpoonacularSearchResponse(
        List<SpoonacularMenuItem> menuItems,
        String type,
        int offset,
        int number
) {}
