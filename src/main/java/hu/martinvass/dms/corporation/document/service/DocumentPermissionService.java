package hu.martinvass.dms.corporation.document.service;

import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.domain.DocumentPermission;
import hu.martinvass.dms.corporation.document.domain.DocumentPermissionLevel;
import hu.martinvass.dms.corporation.document.repository.DocumentPermissionRepository;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.profile.CorporationProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Central service for checking and managing document permissions.
 * <p>
 * Permission hierarchy:
 * 1. Corporation Admin - full access to all documents
 * 2. Document Owner - full access to own documents
 * 3. Explicit Document Permission - user-specific permissions
 * 4. Department Membership - READ access if both user and document are in same department
 * 5. Default - NONE
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

        // 4. Check department membership
        // If both user and document are in the same department, user gets READ access
        for (Department userDept : profile.getDepartments()) {
            if (document.getDepartments().contains(userDept)) {
                return DocumentPermissionLevel.READ;
            }
        }

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
            throw new SecurityException("You do not have permission to do this, required permission: " + required);
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
            CorporationProfile grantedBy
    ) {
        // Check if granter has ADMIN permission
        if (!hasPermission(document, grantedBy, DocumentPermissionLevel.ADMIN)) {
            throw new SecurityException("Cannot grant permissions without ADMIN access");
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
                    .grantedBy(grantedBy.getUser())
                    .build();
        }

        return documentPermissionRepository.save(permission);
    }

    /**
     * Revoke permission from a user
     */
    @Transactional
    public void revokePermission(Document document, CorporationProfile targetProfile, CorporationProfile revokedBy) {
        // Check if revoker has ADMIN permission
        if (!hasPermission(document, revokedBy, DocumentPermissionLevel.ADMIN)) {
            throw new SecurityException("Cannot revoke permissions without ADMIN access");
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

    @Transactional
    public void copyPermissions(Document fromDocument, Document toDocument) {
        // Copy departments - simply create new Set
        if (fromDocument.getDepartments() != null && !fromDocument.getDepartments().isEmpty()) {
            toDocument.setDepartments(new HashSet<>(fromDocument.getDepartments()));
        }

        // Copy explicit permissions
        List<DocumentPermission> sourcePermissions = documentPermissionRepository.findByDocument(fromDocument);
        for (DocumentPermission sourcePerm : sourcePermissions) {
            DocumentPermission newPerm = DocumentPermission.builder()
                    .document(toDocument)
                    .profile(sourcePerm.getProfile())
                    .permissionLevel(sourcePerm.getPermissionLevel())
                    .grantedBy(sourcePerm.getGrantedBy())
                    .build();

            documentPermissionRepository.save(newPerm);
        }
    }
}