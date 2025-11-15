package foodiepass.server.menu.infra.scraper.gemini;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.jupiter.api.Disabled;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("GeminiClient 디버그 테스트 - 실제 응답 확인")
@Disabled("Integration test with external Gemini API - temporarily disabled for test architecture validation")
class GeminiClientDebugTest {

    private static final String TEST_IMAGE_PATH = "src/test/resources/images/test-menu.jpg";

    @Autowired
    private GeminiClient geminiClient;

    @Test
    @DisplayName("Gemini API 응답 원문을 확인한다")
    void checkRawGeminiResponse() throws IOException {
        // given: 테스트용 일본어 메뉴판 이미지
        final Path imagePath = Paths.get(TEST_IMAGE_PATH);
        final byte[] imageBytes = Files.readAllBytes(imagePath);
        final String base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);
        final ByteString byteStringImage = ByteString.copyFrom(Base64.getDecoder().decode(base64EncodedImage));

        final String prompt = GeminiOcrReader.JSON_EXTRACT_PROMPT_MESSAGE;

        System.out.println("=== Calling Gemini API ===");
        System.out.println("Prompt: " + prompt);
        System.out.println("Image size: " + imageBytes.length + " bytes");

        // when: Gemini API 호출
        String response;
        try {
            response = geminiClient.generateText(byteStringImage, "image/jpeg", prompt);
            System.out.println("\n=== Gemini API Response ===");
            System.out.println(response);
            System.out.println("=== End of Response ===");
        } catch (Exception e) {
            System.err.println("=== Gemini API Call Failed ===");
            System.err.println("Error: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
