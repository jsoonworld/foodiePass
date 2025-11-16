package foodiepass.server.cache.repository;

import foodiepass.server.cache.domain.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MenuItemEntity operations.
 */
public interface MenuItemRepository extends JpaRepository<MenuItemEntity, UUID> {

    /**
     * Find all menu items by MenuScan ID.
     *
     * @param menuScanId MenuScan UUID
     * @return List of MenuItemEntity
     */
    List<MenuItemEntity> findByMenuScanId(UUID menuScanId);
}
