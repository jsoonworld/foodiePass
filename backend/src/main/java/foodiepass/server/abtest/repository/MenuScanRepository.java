package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MenuScanRepository extends JpaRepository<MenuScan, UUID> {

    /**
     * 사용자의 가장 최근 스캔 조회
     */
    Optional<MenuScan> findFirstByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * A/B 그룹별 스캔 개수
     */
    long countByAbGroup(ABGroup abGroup);
}
