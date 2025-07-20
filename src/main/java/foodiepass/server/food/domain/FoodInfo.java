package foodiepass.server.food.domain;

import foodiepass.server.food.exception.FoodErrorCode;
import foodiepass.server.food.exception.FoodException;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class FoodInfo {
    private final String name;
    private final String description;
    private final String image;
    private final String previewImage;

    public FoodInfo(String name, String description, String image, String previewImage) {
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
