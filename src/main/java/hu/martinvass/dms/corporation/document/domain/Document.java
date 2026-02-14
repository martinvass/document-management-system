package hu.martinvass.dms.corporation.document.domain;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.corporation.tag.domain.Tag;
import hu.martinvass.dms.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporation_id", nullable = false)
    private Corporation corporation;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storagePath;   // pl: uuid/original.pdf

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String contentType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false)
    @Builder.Default
    private StorageType storageType = StorageType.MANAGED;

    @Column(nullable = false)
    @Builder.Default
    private Integer version = 1;

    /**
     * Reference to the previous version (for version history)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_version_id")
    private Document previousVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "latest_version_id")
    private Document latestVersion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_tags",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private AppUser uploadedBy;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column
    private LocalDateTime modifiedAt;

    @Column
    private LocalDateTime archivedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archived_by")
    private AppUser archivedBy;

    @Column
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private AppUser deletedBy;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        modifiedAt = LocalDateTime.now();
        if (latestVersion == null) {
            latestVersion = this;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = LocalDateTime.now();
    }

    /**
     * Check if this document is the latest version
     */
    public boolean isLatestVersion() {
        return latestVersion == null || latestVersion.getId().equals(this.id);
    }

    /**
     * Check if this document is active (not archived or deleted)
     */
    public boolean isActive() {
        return status == DocumentStatus.ACTIVE && archivedAt == null && deletedAt == null;
    }

    /**
     * Check if this document is archived
     */
    public boolean isArchived() {
        return status == DocumentStatus.ARCHIVED || archivedAt != null;
    }

    /**
     * Check if this document is deleted
     */
    public boolean isDeleted() {
        return status == DocumentStatus.DELETED || deletedAt != null;
    }

    /**
     * Add a tag to this document
     */
    public void addTag(Tag tag) {
        tags.add(tag);
    }

    /**
     * Remove a tag from this document
     */
    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public String getIconClass() {
        return switch (contentType) {
            case "application/pdf" -> "far fa-file-pdf";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "far fa-file-word";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation" -> "far fa-file-powerpoint";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> "far fa-file-excel";
            case "text/plain" -> "far fa-file-lines";
            case "image/jpeg", "image/png" -> "far fa-image";
            default -> "far fa-file";
        };
    }
}
