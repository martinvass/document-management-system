package hu.martinvass.dms.corporation.document.domain;

import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Document-level permission for a user.
 * Defines what a specific user can do with a specific document.
 */
@Entity
@Table(
        name = "document_permissions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "profile_id"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    /**
     * The user profile that has this permission
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CorporationProfile profile;

    /**
     * Permission level granted
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentPermissionLevel permissionLevel;

    /**
     * Who granted this permission
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "granted_by")
    private AppUser grantedBy;

    @Column(nullable = false)
    private LocalDateTime grantedAt;

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}