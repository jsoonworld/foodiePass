package foodiepass.server.menu.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import foodiepass.server.menu.exception.FoodErrorCode;
import foodiepass.server.menu.exception.FoodException;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class FoodInfo {
    private final String name;
    private final String description;
    private final String image;
    private final String previewImage;

    @JsonCreator
    public FoodInfo(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("image") String image,
            @JsonProperty("previewImage") String previewImage
    ) {
        validateName(name);
        this.name = name;
        this.description = description;
        this.image = image;
        this.previewImage = previewImage;
    }

    private void validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new FoodException(FoodErrorCode.INVALID_FOOD_NAME);
        }
    }
}
