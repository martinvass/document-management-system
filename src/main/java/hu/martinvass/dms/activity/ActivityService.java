package hu.martinvass.dms.activity;

import hu.martinvass.dms.activity.repository.ActivityRepository;
import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;

    /**
     * Log an activity
     */
    @Transactional
    public void log(
            Corporation corporation,
            AppUser user,
            ActivityType type,
            String entityName,
            Long entityId
    ) {
        ActivityLog activity = ActivityLog.builder()
                .corporation(corporation)
                .user(user)
                .type(type)
                .entityName(entityName)
                .entityId(entityId)
                .build();

        activityRepository.save(activity);
    }

    /**
     * Log activity with details
     */
    @Transactional
    public void log(
            Corporation corporation,
            AppUser user,
            ActivityType type,
            String entityName,
            Long entityId,
            String details
    ) {
        ActivityLog activity = ActivityLog.builder()
                .corporation(corporation)
                .user(user)
                .type(type)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .build();

        activityRepository.save(activity);
    }

    /**
     * Get recent activities (for dashboard)
     */
    @Transactional(readOnly = true)
    public List<ActivityLog> getRecentActivities(Corporation corporation, int limit) {
        return activityRepository.findTop10ByCorporationOrderByCreatedAtDesc(corporation)
                .stream()
                .limit(limit)
                .toList();
    }
}
