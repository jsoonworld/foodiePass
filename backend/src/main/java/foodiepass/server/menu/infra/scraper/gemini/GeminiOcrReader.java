package foodiepass.server.menu.infra.scraper.gemini;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.application.port.out.OcrReader;
import foodiepass.server.menu.domain.MenuItem;
import foodiepass.server.menu.infra.exception.GeminiErrorCode;
import foodiepass.server.menu.infra.exception.GeminiException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile(ProfileConstants.NOT_TEST_AND_NOT_PERFORMANCE_TEST)
public class GeminiOcrReader implements OcrReader {

    public static final String JSON_EXTRACT_PROMPT_MESSAGE = "Given a menu image, please extract and print the names and prices of the food items in JSON format. Follow the structure below for each item:\n\n[{\"name\": \"Name of the Food (String)\", \"price\": Price of the Food (double)}, ...]";
    private static final String IMAGE_MIME_TYPE = "image/jpeg";

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

            return objectMapper.readValue(jsonResponse, new TypeReference<>() {});

        } catch (final JsonProcessingException e) {
            throw new GeminiException(GeminiErrorCode.OCR_REQUEST_FAILED);
        }
    }
}
