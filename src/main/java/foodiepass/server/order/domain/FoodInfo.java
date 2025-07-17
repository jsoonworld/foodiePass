package foodiepass.server.order.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FoodInfo {
    private final String description;
    private final String image;
    private final String previewImage;
}
