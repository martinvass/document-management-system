package hu.martinvass.dms.activity;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_activity_corporation", columnList = "corporation_id,created_at"),
        @Index(name = "idx_activity_user", columnList = "user_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporation_id", nullable = false)
    private Corporation corporation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityType type;

    /**
     * Entity name (e.g., document filename, department name, username)
     */
    @Column(name = "entity_name", length = 500)
    private String entityName;

    /**
     * Entity ID (for reference)
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * Additional details (JSON or plain text)
     */
    @Column(length = 1000)
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Get formatted activity message
     */
    public String getFormattedMessage() {
        String userName = user.getProfile().getUsername();
        String action = type.getDisplayText();

        if (entityName != null) {
            return userName + " " + action + " " + entityName;
        }

        return userName + " " + action;
    }
}