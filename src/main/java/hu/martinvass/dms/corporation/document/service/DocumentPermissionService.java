package hu.martinvass.dms.corporation.document.service;

import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.domain.DocumentPermission;
import hu.martinvass.dms.corporation.document.domain.DocumentPermissionLevel;
import hu.martinvass.dms.corporation.document.repository.DocumentPermissionRepository;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// TODO: custom exception handling
/**
 * Central service for checking and managing document permissions.
 * <p>
 * Permission hierarchy:
 * 1. Corporation Admin - full access to all documents
 * 2. Document Owner - full access to own documents
 * 3. Explicit Document Permission - user-specific permissions
 * 4. Default by Role:
 *    - EMPLOYEE: access to own documents only
 *    - GUEST: no default access
 */
@Service
@RequiredArgsConstructor
public class DocumentPermissionService {

    private final DocumentPermissionRepository documentPermissionRepository;

    /**
     * Get the effective permission level for a user on a document.
     * Returns the highest permission level from all sources.
     */
    @Transactional(readOnly = true)
    public DocumentPermissionLevel getEffectivePermission(Document document, CorporationProfile profile) {
        // 1. Corporation Admin has full access
        if (profile.getRole() == CorporationRole.ADMIN) {
            return DocumentPermissionLevel.ADMIN;
        }

        // 2. Document owner has full access
        if (document.getUploadedBy().getId().equals(profile.getUser().getId())) {
            return DocumentPermissionLevel.ADMIN;
        }

        // 3. Check explicit document permission
        Optional<DocumentPermission> explicitPermission =
                documentPermissionRepository.findByDocumentAndProfile(document, profile);

        if (explicitPermission.isPresent()) {
            return explicitPermission.get().getPermissionLevel();
        }

        // 4. Default by role - no default access
        return DocumentPermissionLevel.NONE;
    }

    /**
     * Check if user has at least the required permission level
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Document document, CorporationProfile profile, DocumentPermissionLevel required) {
        DocumentPermissionLevel effective = getEffectivePermission(document, profile);
        return effective.includes(required);
    }

    /**
     * Check permission and throw exception if not sufficient
     */
    @Transactional(readOnly = true)
    public void checkPermission(Document document, CorporationProfile profile, DocumentPermissionLevel required) {
        if (!hasPermission(document, profile, required)) {
            throw new RuntimeException("You do not have permission to do this, required permission: " + required);
        }
    }

    /**
     * Grant explicit permission to a user
     */
    @Transactional
    public DocumentPermission grantPermission(
            Document document,
            CorporationProfile targetProfile,
            DocumentPermissionLevel level,
            AppUser grantedBy
    ) {
        // Check if granter has ADMIN permission
        if (!hasPermission(document, grantedBy.getActiveProfile(), DocumentPermissionLevel.ADMIN)) {
            throw new RuntimeException("Cannot grant permissions without ADMIN access");
        }

        // Check if permission already exists
        Optional<DocumentPermission> existing =
                documentPermissionRepository.findByDocumentAndProfile(document, targetProfile);

        DocumentPermission permission;
        if (existing.isPresent()) {
            // Update existing permission
            permission = existing.get();
            permission.setPermissionLevel(level);
        } else {
            // Create new permission
            permission = DocumentPermission.builder()
                    .document(document)
                    .profile(targetProfile)
                    .permissionLevel(level)
                    .grantedBy(grantedBy)
                    .build();
        }

        return documentPermissionRepository.save(permission);
    }

    /**
     * Revoke permission from a user
     */
    @Transactional
    public void revokePermission(Document document, CorporationProfile targetProfile, AppUser revokedBy) {
        // Check if revoker has ADMIN permission
        if (!hasPermission(document, revokedBy.getActiveProfile(), DocumentPermissionLevel.ADMIN)) {
            throw new RuntimeException("Cannot revoke permissions without ADMIN access");
        }

        documentPermissionRepository.deleteByDocumentAndProfile(document, targetProfile);
    }

    /**
     * Get all users with explicit permissions to a document
     */
    @Transactional(readOnly = true)
    public List<DocumentPermission> getDocumentPermissions(Document document, CorporationProfile requester) {
        // Check if requester has READ permission
        checkPermission(document, requester, DocumentPermissionLevel.READ);

        return documentPermissionRepository.findByDocument(document);
    }

    /**
     * Check if user can upload documents (based on corporation role)
     */
    public boolean canUploadDocuments(CorporationProfile profile) {
        return profile.getRole() == CorporationRole.ADMIN ||
                profile.getRole() == CorporationRole.EMPLOYEE;
    }
}