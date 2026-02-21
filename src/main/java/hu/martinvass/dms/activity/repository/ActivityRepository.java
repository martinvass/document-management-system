package hu.martinvass.dms.activity.repository;

import hu.martinvass.dms.activity.ActivityLog;
import hu.martinvass.dms.corporation.domain.Corporation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<ActivityLog, Long> {

    /**
     * Find recent activities in a corporation
     */
    List<ActivityLog> findTop10ByCorporationOrderByCreatedAtDesc(Corporation corporation);

    /**
     * Find activities in a corporation (paginated)
     */
    Page<ActivityLog> findByCorporationOrderByCreatedAtDesc(Corporation corporation, Pageable pageable);
}
