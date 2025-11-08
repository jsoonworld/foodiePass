package foodiepass.server.menu.infra.scraper.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Gemini OCR API 응답을 매핑하기 위한 DTO.
 * price 필드는 API 응답 형식(double)을 따르며, 도메인 객체 변환 시 BigDecimal로 변환됩니다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemOcrDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("price")
    private double price;
}
