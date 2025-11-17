package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.ABGroup;
import foodiepass.server.abtest.domain.MenuScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    /**
     * A/B 그룹별 스캔 개수를 한 번의 쿼리로 조회
     * 데이터 일관성을 보장하기 위해 GROUP BY를 사용
     */
    @Query("SELECT m.abGroup, COUNT(m) FROM MenuScan m GROUP BY m.abGroup")
    List<Object[]> countGroupByAbGroup();

    /**
     * imageHash로 MenuScan 조회 (캐시 히트 체크용)
     */
    Optional<MenuScan> findByImageHash(String imageHash);
}
