package hu.martinvass.dms.document.repository;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.document.domain.Document;
import hu.martinvass.dms.document.domain.DocumentStatus;
import hu.martinvass.dms.user.domain.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

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

    List<Document> findByCorporationAndStatus(
            Corporation corporation,
            DocumentStatus status
    );

    @Query("SELECT d FROM Document d WHERE d.corporation = :corporation AND d.status = :status " +
            "AND d.latestVersion = d ORDER BY d.uploadedAt DESC")
    List<Document> findLatestVersionsByCorporationAndStatus(
            @Param("corporation") Corporation corporation,
            @Param("status") DocumentStatus status
    );

    @Query("SELECT d FROM Document d WHERE d.corporation = :corporation AND d.status = :status " +
            "AND d.latestVersion = d ORDER BY d.uploadedAt DESC")
    Page<Document> findLatestVersionsByCorporationAndStatus(
            @Param("corporation") Corporation corporation,
            @Param("status") DocumentStatus status,
            Pageable pageable
    );

    long countByCorporationAndStatus(Corporation corporation, DocumentStatus status);

    List<Document> findByUploadedByOrderByUploadedAtDesc(AppUser user);

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

    @Modifying
    @Query("UPDATE Document d SET d.latestVersion = :newLatest " +
            "WHERE d = :current OR d.latestVersion = :current")
    void updateLatestVersionForAll(@Param("current") Document current,
                                   @Param("newLatest") Document newLatest);

    /**
     * Get document upload trend for last N days
     */
    @Query("SELECT d.uploadedAt, d.id " +
            "FROM Document d " +
            "WHERE d.corporation.id = :corporationId " +
            "AND d.uploadedAt >= :fromDate " +
            "ORDER BY d.uploadedAt ASC")
    List<Object[]> getUploadTrendsByDays(@Param("corporationId") Long corporationId,
                                         @Param("fromDate") LocalDateTime fromDate);

    /**
     * Get file type distribution
     */
    @Query("SELECT " +
            "CASE " +
            "  WHEN d.contentType LIKE '%image%' THEN 'Images' " +
            "  WHEN d.contentType LIKE '%pdf%' THEN 'PDF' " +
            "  WHEN d.contentType LIKE '%word%' OR d.contentType LIKE '%wordprocessingml%' THEN 'Word' " +
            "  WHEN d.contentType LIKE '%excel%' OR d.contentType LIKE '%spreadsheetml%' THEN 'Excel' " +
            "  WHEN d.contentType LIKE '%powerpoint%' OR d.contentType LIKE '%presentationml%' THEN 'PowerPoint' " +
            "  WHEN d.contentType LIKE '%text/plain%' THEN 'Text' " +
            "  WHEN d.contentType LIKE '%csv%' THEN 'CSV' " +
            "  ELSE 'Other' " +
            "END, " +
            "COUNT(d), " +
            "SUM(d.size) " +
            "FROM Document d " +
            "WHERE d.corporation.id = :corporationId " +
            "GROUP BY " +
            "CASE " +
            "  WHEN d.contentType LIKE '%image%' THEN 'Images' " +
            "  WHEN d.contentType LIKE '%pdf%' THEN 'PDF' " +
            "  WHEN d.contentType LIKE '%word%' OR d.contentType LIKE '%wordprocessingml%' THEN 'Word' " +
            "  WHEN d.contentType LIKE '%excel%' OR d.contentType LIKE '%spreadsheetml%' THEN 'Excel' " +
            "  WHEN d.contentType LIKE '%powerpoint%' OR d.contentType LIKE '%presentationml%' THEN 'PowerPoint' " +
            "  WHEN d.contentType LIKE '%text/plain%' THEN 'Text' " +
            "  WHEN d.contentType LIKE '%csv%' THEN 'CSV' " +
            "  ELSE 'Other' " +
            "END " +
            "ORDER BY COUNT(d) DESC")
    List<Object[]> getFileTypeDistribution(@Param("corporationId") Long corporationId);

    /**
     * Get top document uploaders
     */
    @Query("SELECT u.profile.username, COUNT(d) " +
            "FROM Document d " +
            "JOIN d.uploadedBy u " +
            "WHERE d.corporation.id = :corporationId " +
            "GROUP BY u.profile.username " +
            "ORDER BY COUNT(d) DESC " +
            "LIMIT :limit")
    List<Object[]> getTopUploaders(@Param("corporationId") Long corporationId, @Param("limit") int limit);

    /**
     * Get document count by department
     */
    @Query("SELECT dept.name, COUNT(DISTINCT d) " +
            "FROM Document d " +
            "JOIN d.departments dept " +
            "WHERE dept.corporation.id = :corporationId " +
            "GROUP BY dept.id, dept.name " +
            "ORDER BY COUNT(DISTINCT d) DESC")
    List<Object[]> getDocumentsByDepartment(@Param("corporationId") Long corporationId);

    long countByUploadedByAndCorporationAndStatus(
            AppUser uploadedBy,
            Corporation corporation,
            DocumentStatus status
    );
}