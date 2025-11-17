package foodiepass.server.menu.dto.request;

public record ReconfigureRequest(
        String base64EncodedImage,
        String originLanguageName,
        String userLanguageName,
        String originCurrencyName,
        String userCurrencyName
) {
}
