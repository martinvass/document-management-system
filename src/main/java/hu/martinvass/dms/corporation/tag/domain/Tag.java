package hu.martinvass.dms.corporation.tag.domain;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tag entity for categorizing documents.
 * Tags are corporation-specific and normalized.
 */
@Entity
@Table(
        name = "tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"corporation_id", "name"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporation_id", nullable = false)
    private Corporation corporation;

    /**
     * Normalized tag name (lowercase, trimmed)
     */
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(length = 7)
    private String color;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (name != null) {
            name = name.trim().toLowerCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (name != null) {
            name = name.trim().toLowerCase();
        }
    }
}