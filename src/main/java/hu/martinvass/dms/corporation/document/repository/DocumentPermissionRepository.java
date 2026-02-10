package hu.martinvass.dms.corporation.document.repository;

import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.domain.DocumentPermission;
import hu.martinvass.dms.profile.CorporationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {

    /**
     * Find permission for a specific user and document
     */
    Optional<DocumentPermission> findByDocumentAndProfile(Document document, CorporationProfile profile);

    /**
     * Find all permissions for a document
     */
    List<DocumentPermission> findByDocument(Document document);

    /**
     * Delete all permissions for a document
     */
    void deleteByDocument(Document document);

    /**
     * Delete permission for a specific user
     */
    void deleteByDocumentAndProfile(Document document, CorporationProfile profile);
}