package foodiepass.server.menu.application.port.out;

import foodiepass.server.menu.domain.MenuItem;

import java.util.List;

/**
 * OCR을 통해 메뉴판 이미지에서 메뉴 항목을 추출하는 인터페이스
 */
public interface OcrReader {

    /**
     * Base64 인코딩된 이미지에서 메뉴 항목 목록을 추출합니다.
     *
     * @param base64encodedImage Base64로 인코딩된 메뉴판 이미지
     * @return 추출된 메뉴 항목 목록
     */
    List<MenuItem> read(String base64encodedImage);
}
