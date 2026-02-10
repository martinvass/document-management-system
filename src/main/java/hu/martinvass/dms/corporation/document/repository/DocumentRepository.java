package hu.martinvass.dms.corporation.document.repository;

import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.domain.DocumentStatus;
import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.user.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    /**
     * Find active documents in a corporation (paginated)
     */
    Page<Document> findByCorporationAndStatusOrderByUploadedAtDesc(
            Corporation corporation,
            DocumentStatus status,
            Pageable pageable
    );

    /**
     * Find active documents in a corporation
     */
    List<Document> findByCorporationAndStatusOrderByUploadedAtDesc(
            Corporation corporation,
            DocumentStatus status
    );

    /**
     * Find document by ID and corporation (for security check)
     */
    Optional<Document> findByIdAndCorporation(Long id, Corporation corporation);

    /**
     * Find documents uploaded by a user
     */
    Page<Document> findByUploadedByAndStatusOrderByUploadedAtDesc(
            AppUser user,
            DocumentStatus status,
            Pageable pageable
    );

    /**
     * Find all versions of a document
     */
    @Query("SELECT d FROM Document d WHERE d.latestVersion = :latest ORDER BY d.version DESC")
    List<Document> findAllVersions(@Param("latest") Document latestVersion);

    /**
     * Find latest version of a document
     */
    @Query("SELECT d.latestVersion FROM Document d WHERE d.id = :docId")
    Optional<Document> findLatestVersion(@Param("docId") Long documentId);
}