package foodiepass.server.abtest.repository;

import foodiepass.server.abtest.domain.MenuScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for managing MenuScan entities.
 */
@Repository
public interface MenuScanRepository extends JpaRepository<MenuScan, UUID> {
}
