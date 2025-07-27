package foodiepass.server.menu.infra.scraper.gemini;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.protobuf.ByteString;
import foodiepass.server.global.config.ProfileConstants;
import foodiepass.server.menu.infra.exception.GeminiErrorCode;
import foodiepass.server.menu.infra.exception.GeminiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@Profile(ProfileConstants.NOT_PERFORMANCE_TEST)
public class GeminiClient {

    private final GenerativeModel multimodalModel;
    private final GenerativeModel textModel;
    private final GeminiResponseParser responseParser;

    public GeminiClient(
            final VertexAI vertexAI,
            final GeminiResponseParser responseParser,
            @Value("${google.gemini.model.vision}") final String multimodalModelName,
            @Value("${google.gemini.model.pro}") final String textModelName
    ) {
        this.multimodalModel = new GenerativeModel(multimodalModelName, vertexAI);
        this.textModel = new GenerativeModel(textModelName, vertexAI);
        this.responseParser = responseParser;
    }

    public String generateText(final String prompt) {
        try {
            final GenerateContentResponse apiResponse = textModel.generateContent(ContentMaker.fromString(prompt));
            return extractAndParseText(apiResponse);
        } catch (IOException e) {
            throw new GeminiException(GeminiErrorCode.GEMINI_API_IO_ERROR);
        }
    }

    public String generateText(final ByteString imageBytes, final String mimeType, final String prompt) {
        try {
            final GenerateContentResponse apiResponse = multimodalModel.generateContent(
                    ContentMaker.fromMultiModalData(
                            PartMaker.fromMimeTypeAndData(mimeType, imageBytes),
                            prompt
                    ));
            return extractAndParseText(apiResponse);
        } catch (IOException e) {
            throw new GeminiException(GeminiErrorCode.GEMINI_API_IO_ERROR);
        }
    }

    private String extractAndParseText(final GenerateContentResponse apiResponse) {
        final String extractedText = Optional.of(apiResponse)
                .filter(response -> !response.getCandidatesList().isEmpty())
                .map(response -> response.getCandidates(0))
                .filter(candidate -> !candidate.getContent().getPartsList().isEmpty())
                .map(candidate -> candidate.getContent().getParts(0).getText())
                .filter(text -> !text.isBlank())
                .orElseThrow(() -> new GeminiException(GeminiErrorCode.INVALID_GEMINI_RESPONSE));

        return responseParser.parse(extractedText);
    }
}
