package foodiepass.server.menu.infra.scraper.tasteAtlas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TasteAtlasResponse(
        @JsonProperty("CustomItems") List<Item> customItems,
        @JsonProperty("Items") List<Item> items
) {
    public record Item(
            @JsonProperty("Name") String name,
            @JsonProperty("OtherName") String otherName,
            @JsonProperty("Subtitle") String subtitle,
            @JsonProperty("PreviewImage") PreviewImage previewImage,
            @JsonProperty("UrlLink") String urlLink
    ) {}

    public record PreviewImage(
            @JsonProperty("Image") String image
    ) {}
}
