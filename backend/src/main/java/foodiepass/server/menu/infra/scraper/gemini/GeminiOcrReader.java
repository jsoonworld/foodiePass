package foodiepass.server.menu.infra.scraper.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import foodiepass.server.common.price.domain.Price;
import foodiepass.server.currency.domain.Currency;
import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.infra.exception.GeminiErrorCode;
import foodiepass.server.menu.infra.exception.GeminiException;
import foodiepass.server.menu.infra.scraper.gemini.dto.MenuItemOcrDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile(ProfileConstants.NOT_TEST_AND_NOT_PERFORMANCE_TEST)
public class GeminiOcrReader implements OcrReader {

    public static final String JSON_EXTRACT_PROMPT_MESSAGE = "Given a menu image, please extract and print the names and prices of the food items in JSON format. Follow the structure below for each item:\n\n[{\"name\": \"Name of the Food (String)\", \"price\": Price of the Food (double)}, ...]";
    private static final String IMAGE_MIME_TYPE = "image/jpeg";
    private static final Currency DEFAULT_CURRENCY = Currency.JAPANESE_YEN;

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<MenuItem> read(final String base64encodedImage) {
        try {
            final ByteString byteStringImage = ByteString.copyFrom(
                    Base64.getDecoder().decode(base64encodedImage)
            );

            final String jsonResponse = geminiClient.generateText(
                    byteStringImage,
                    IMAGE_MIME_TYPE,
                    JSON_EXTRACT_PROMPT_MESSAGE
            );

            log.debug("Gemini OCR response: {}", jsonResponse);

            // Parse JSON to DTO
            final List<MenuItemOcrDto> dtos = objectMapper.readValue(jsonResponse, new TypeReference<>() {});

            // Convert DTOs to MenuItem domain objects
            return dtos.stream()
                    .map(this::convertToMenuItem)
                    .collect(Collectors.toList());

        } catch (final JsonProcessingException e) {
            log.error("Failed to parse Gemini OCR response", e);
            throw new GeminiException(GeminiErrorCode.OCR_REQUEST_FAILED);
        }
    }

    private MenuItem convertToMenuItem(MenuItemOcrDto dto) {
        final Price price = new Price(DEFAULT_CURRENCY, BigDecimal.valueOf(dto.getPrice()));
        return new MenuItem(dto.getName(), price, null);
    }
}
