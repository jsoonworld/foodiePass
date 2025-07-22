package foodiepass.server.menu.infra.scraper.tasteAtlas;

import foodiepass.server.menu.domain.FoodInfo;
import foodiepass.server.menu.infra.config.TasteAtlasProperties;
import foodiepass.server.menu.infra.scraper.tasteAtlas.dto.TasteAtlasResponse;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TasteAtlasPageParser {

    private final TasteAtlasProperties properties;

    public Mono<FoodInfo> parse(String html, TasteAtlasResponse.Item item) {
        return Mono.fromCallable(() -> Jsoup.parse(html))
                .subscribeOn(Schedulers.boundedElastic())
                .map(doc -> extractFoodInfo(doc, item));
    }

    private FoodInfo extractFoodInfo(Document doc, TasteAtlasResponse.Item item) {
        String foodDescription = getMetaTagContent(doc, properties.selector().description())
                .orElse(properties.defaults().description());

        String foodImageUrl = getMetaTagContent(doc, properties.selector().image())
                .orElse(properties.defaults().imageUrl());

        String previewImageUrl = Optional.ofNullable(item.previewImage())
                .map(TasteAtlasResponse.PreviewImage::image)
                .orElse(properties.defaults().imageUrl());

        return new FoodInfo(generateFullName(item), foodDescription, foodImageUrl, previewImageUrl);
    }

    private String generateFullName(TasteAtlasResponse.Item item) {
        if (StringUtils.hasText(item.subtitle())) {
            return String.format("%s (%s)", item.name(), item.subtitle());
        }
        return item.name();
    }

    private Optional<String> getMetaTagContent(Document document, String selector) {
        return Optional.ofNullable(document.select(selector).first())
                .map(element -> element.attr("content"));
    }
}
