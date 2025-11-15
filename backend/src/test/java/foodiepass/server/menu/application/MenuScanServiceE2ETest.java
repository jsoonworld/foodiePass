package foodiepass.server.menu.application;

import foodiepass.server.menu.dto.request.MenuScanRequest;
import foodiepass.server.menu.dto.response.MenuScanResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("integration-test")
@DisplayName("MenuScan E2E 통합 테스트 - 전체 플로우 (A/B 테스트 포함)")
@Disabled("E2E test with external APIs - temporarily disabled for test architecture validation")
class MenuScanServiceE2ETest {

    private static final String TEST_IMAGE_PATH = "src/test/resources/images/test-menu.jpg";

    @Autowired
    private MenuScanService menuScanService;

    private String base64EncodedImage;

    @BeforeEach
    void setUp() throws IOException {
        assumeTrue(
            System.getenv("GEMINI_API_KEY") != null && !System.getenv("GEMINI_API_KEY").isBlank(),
            "GEMINI_API_KEY 환경 변수가 설정된 경우에만 통합 테스트를 실행합니다."
        );

        final Path imagePath = Paths.get(TEST_IMAGE_PATH);
        byte[] imageBytes = Files.readAllBytes(imagePath);
        base64EncodedImage = Base64.getEncoder().encodeToString(imageBytes);
    }

    @Test
    @DisplayName("메뉴판 업로드 → 전체 처리 → A/B 분기")
    void scanMenu_fullPipeline_withABTest() {
        // Given: 실제 메뉴판 이미지
        MenuScanRequest request = new MenuScanRequest(
            base64EncodedImage,
            "ja",
            "ko",
            "Japanese Yen",
            "South Korean won"
        );

        String userId = "test-user-e2e-" + System.currentTimeMillis();

        System.out.println("=== Starting E2E Test with A/B Test ===");
        System.out.println("User ID: " + userId);

        // When: 전체 플로우 실행
        var result = menuScanService.scanMenu(request, userId);

        // Then: 검증
        StepVerifier.create(result)
            .assertNext(response -> {
                System.out.println("\n=== E2E 테스트 결과 ===");
                System.out.println("Scan ID: " + response.scanId());
                System.out.println("A/B Group: " + response.abGroup());
                System.out.println("Processing Time: " + response.processingTime() + "s");
                System.out.println("Items: " + response.items().size());

                // 기본 검증
                assertThat(response.scanId()).isNotNull();
                assertThat(response.abGroup()).isIn("CONTROL", "TREATMENT");
                assertThat(response.items()).isNotEmpty();
                assertThat(response.processingTime()).isLessThan(15.0); // 15초 이내 (느슨한 검증)

                // H2 검증: 처리 시간 5초 이내 목표
                if (response.processingTime() <= 5.0) {
                    System.out.println("✅ H2 검증 성공: 처리 시간 5초 이내");
                } else {
                    System.out.println("⚠️ H2 검증 경고: 처리 시간 5초 초과 (목표: 5초, 실제: " + response.processingTime() + "초)");
                }

                // A/B 그룹별 검증
                if (response.abGroup().equals("TREATMENT")) {
                    // Treatment: 사진 + 설명 있어야 함
                    response.items().forEach(item -> {
                        System.out.println("\n--- Treatment 그룹 메뉴 ---");
                        System.out.println("원본: " + item.originalName());
                        System.out.println("번역: " + item.translatedName());
                        System.out.println("이미지: " + (item.imageUrl() != null ? "있음" : "없음"));
                        System.out.println("설명: " + (item.description() != null ? "있음 (" + item.description().length() + "자)" : "없음"));

                        // Treatment는 이미지와 설명이 있어야 함
                        assertThat(item.imageUrl())
                            .as("Treatment 그룹은 이미지가 있어야 함")
                            .isNotNull();
                        assertThat(item.description())
                            .as("Treatment 그룹은 설명이 있어야 함")
                            .isNotNull();
                    });
                    System.out.println("✅ Treatment 그룹: 사진 + 설명 포함");
                } else {
                    // Control: 사진 + 설명 없어야 함
                    response.items().forEach(item -> {
                        System.out.println("\n--- Control 그룹 메뉴 ---");
                        System.out.println("원본: " + item.originalName());
                        System.out.println("번역: " + item.translatedName());
                        System.out.println("이미지: " + (item.imageUrl() != null ? "있음" : "없음"));
                        System.out.println("설명: " + (item.description() != null ? "있음" : "없음"));

                        // Control은 이미지와 설명이 없어야 함
                        assertThat(item.imageUrl())
                            .as("Control 그룹은 이미지가 없어야 함")
                            .isNull();
                        assertThat(item.description())
                            .as("Control 그룹은 설명이 없어야 함")
                            .isNull();
                    });
                    System.out.println("✅ Control 그룹: 텍스트만 포함");
                }

                // 모든 메뉴 아이템 검증
                response.items().forEach(item -> {
                    assertThat(item.originalName()).isNotBlank();
                    assertThat(item.translatedName()).isNotBlank();
                    assertThat(item.priceInfo()).isNotNull();
                    assertThat(item.priceInfo().convertedFormatted()).isNotBlank();
                });
            })
            .verifyComplete();
    }

    @Test
    @DisplayName("여러 사용자의 A/B 그룹 배정 균형 확인")
    void scanMenu_multipleUsers_ABGroupBalance() throws IOException {
        // Given: 동일한 메뉴판 이미지
        MenuScanRequest request = new MenuScanRequest(
            base64EncodedImage,
            "ja",
            "ko",
            "Japanese Yen",
            "South Korean won"
        );

        System.out.println("=== Starting A/B Group Balance Test ===");

        // When: 10명의 사용자로 테스트
        int controlCount = 0;
        int treatmentCount = 0;

        for (int i = 0; i < 10; i++) {
            String userId = "user-balance-" + i + "-" + System.currentTimeMillis();
            MenuScanResponse result = menuScanService.scanMenu(request, userId).block();

            assertThat(result).isNotNull();

            if (result.abGroup().equals("CONTROL")) {
                controlCount++;
            } else {
                treatmentCount++;
            }

            // 각 사용자 결과 출력
            System.out.printf("User %d: Group=%s, Items=%d%n",
                i, result.abGroup(), result.items().size());
        }

        // Then: 대략 50:50 비율
        System.out.println("\n=== A/B 그룹 배정 결과 (10명) ===");
        System.out.println("Control: " + controlCount + " (" + (controlCount * 10) + "%)");
        System.out.println("Treatment: " + treatmentCount + " (" + (treatmentCount * 10) + "%)");

        // 최소 20%는 각 그룹에 배정되어야 함 (통계적 유의성)
        assertThat(controlCount)
            .as("Control 그룹 최소 2명 이상")
            .isGreaterThanOrEqualTo(2);
        assertThat(treatmentCount)
            .as("Treatment 그룹 최소 2명 이상")
            .isGreaterThanOrEqualTo(2);

        System.out.println("✅ A/B 그룹 배정 균형 확인 완료");
    }

    @Test
    @DisplayName("동일 사용자의 A/B 그룹 유지 확인")
    void scanMenu_sameUser_maintainsSameGroup() {
        // Given: 동일한 사용자 ID
        String userId = "user-same-group-" + System.currentTimeMillis();

        MenuScanRequest request = new MenuScanRequest(
            base64EncodedImage,
            "ja",
            "ko",
            "Japanese Yen",
            "South Korean won"
        );

        System.out.println("=== Starting Same User Group Test ===");
        System.out.println("User ID: " + userId);

        // When: 동일 사용자로 3번 요청
        MenuScanResponse result1 = menuScanService.scanMenu(request, userId).block();
        MenuScanResponse result2 = menuScanService.scanMenu(request, userId).block();
        MenuScanResponse result3 = menuScanService.scanMenu(request, userId).block();

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result3).isNotNull();

        System.out.println("첫 번째 요청: Group=" + result1.abGroup());
        System.out.println("두 번째 요청: Group=" + result2.abGroup());
        System.out.println("세 번째 요청: Group=" + result3.abGroup());

        // Then: 모든 요청에서 동일한 그룹
        assertThat(result1.abGroup())
            .as("동일 사용자는 동일한 A/B 그룹을 유지해야 함")
            .isEqualTo(result2.abGroup())
            .isEqualTo(result3.abGroup());

        System.out.println("✅ 동일 사용자 A/B 그룹 유지 확인 완료");
    }

    @Test
    @DisplayName("H2 검증: 전체 파이프라인 성능 측정")
    void scanMenu_h2Verification_performanceCheck() {
        // Given
        MenuScanRequest request = new MenuScanRequest(
            base64EncodedImage,
            "ja",
            "ko",
            "Japanese Yen",
            "South Korean won"
        );

        String userId = "user-performance-" + System.currentTimeMillis();

        System.out.println("=== Starting H2 Performance Verification ===");

        // When
        MenuScanResponse result = menuScanService.scanMenu(request, userId).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.items()).isNotEmpty();

        double processingTime = result.processingTime();
        System.out.println("Processing Time: " + processingTime + "s");

        // H2 가설: 처리 시간 ≤ 5초 (목표)
        // 현실적으로 10초 이내 검증 (외부 API 의존)
        assertThat(processingTime)
            .as("처리 시간이 10초 이내여야 함 (목표: 5초)")
            .isLessThan(10.0);

        if (processingTime <= 5.0) {
            System.out.println("✅ H2 검증 성공: 처리 시간 5초 이내");
        } else if (processingTime <= 10.0) {
            System.out.println("⚠️ H2 검증 경고: 처리 시간 5초 초과하지만 10초 이내");
        } else {
            System.out.println("❌ H2 검증 실패: 처리 시간 10초 초과");
        }
    }
}
