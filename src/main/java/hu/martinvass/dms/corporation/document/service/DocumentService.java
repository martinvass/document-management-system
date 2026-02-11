package hu.martinvass.dms.corporation.document.service;

import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.domain.DocumentPermissionLevel;
import hu.martinvass.dms.corporation.document.domain.DocumentStatus;
import hu.martinvass.dms.corporation.document.repository.DocumentRepository;
import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.storage.StorageProvider;
import hu.martinvass.dms.corporation.storage.StorageRouter;
import hu.martinvass.dms.corporation.storage.StoredFile;
import hu.martinvass.dms.corporation.tag.domain.Tag;
import hu.martinvass.dms.corporation.tag.service.TagService;
import hu.martinvass.dms.profile.CorporationProfile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageRouter storageRouter;
    private final DocumentPermissionService permissionService;
    //private final TagService tagService;

    // File validation constants
    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "text/csv",
            "application/zip",
            "application/x-zip-compressed"
    );

    /**
     * Upload a new document
     */
    @Transactional
    public Document upload(
            CorporationProfile profile,
            MultipartFile file,
            String description,
            Set<String> tagNames
    ) throws IOException {

        // Check if user can upload
        if (!permissionService.canUploadDocuments(profile)) {
            throw new SecurityException("User does not have permission to upload documents");
        }

        // Validate file
        validateFile(file);

        Corporation corporation = profile.getCorporation();

        // Generate storage path
        String storagePath = UUID.randomUUID() + "/" + file.getOriginalFilename();

        // Save to storage
        StorageProvider storage = storageRouter.forCompany(corporation.getId());
        StoredFile stored = storage.save(
                corporation.getId(),
                storagePath,
                file.getInputStream(),
                file.getContentType()
        );

        // Create document entity
        Document document = Document.builder()
                .corporation(corporation)
                .originalFilename(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(stored.size())
                .contentType(file.getContentType())
                .description(description)
                .uploadedBy(profile.getUser())
                .status(DocumentStatus.ACTIVE)
                .build();

        // Add tags
        /*if (tagNames != null && !tagNames.isEmpty()) {
            Set<Tag> tags = tagNames.stream()
                    .map(tagName -> tagService.getOrCreateTag(corporation, tagName, profile.getUser()))
                    .collect(Collectors.toSet());
            document.setTags(tags);
        }*/

        return documentRepository.save(document);
    }

    /**
     * List all active documents
     */
    @Transactional(readOnly = true)
    public List<Document> listDocuments(CorporationProfile profile) {
        Corporation corporation = profile.getCorporation();
        return documentRepository.findByCorporationAndStatusOrderByUploadedAtDesc(
                corporation,
                DocumentStatus.ACTIVE
        );
    }

    /**
     * Get a single document
     */
    @Transactional(readOnly = true)
    public Document getDocument(Long documentId, CorporationProfile profile) {
        Document document = getDocumentEntity(documentId, profile);

        // Check READ permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.READ);

        return document;
    }

    /**
     * Update document metadata
     */
    @Transactional
    public Document updateDocument(
            Long documentId,
            CorporationProfile profile,
            String description,
            Set<String> tagNames
    ) {
        Document document = getDocumentEntity(documentId, profile);

        // Check EDIT permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.EDIT);

        // Update description
        if (description != null) {
            document.setDescription(description);
        }

        // Update tags
        /*if (tagNames != null) {
            document.getTags().clear();
            Set<Tag> newTags = tagNames.stream()
                    .map(tagName -> tagService.getOrCreateTag(profile.getCorporation(), tagName, profile.getUser()))
                    .collect(Collectors.toSet());
            document.setTags(newTags);
        }*/

        return documentRepository.save(document);
    }

    /**
     * Download document
     */
    @Transactional(readOnly = true)
    public InputStream download(Long documentId, CorporationProfile profile) throws IOException {
        Document document = getDocumentEntity(documentId, profile);

        // Check READ permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.READ);

        StorageProvider storage = storageRouter.forCompany(document.getCorporation().getId());

        return storage.load(
                document.getCorporation().getId(),
                document.getStoragePath()
        );
    }

    /**
     * Archive a document (soft delete)
     */
    @Transactional
    public void archiveDocument(Long documentId, CorporationProfile profile) {
        Document document = getDocumentEntity(documentId, profile);

        // Check DELETE permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.DELETE);

        document.setStatus(DocumentStatus.ARCHIVED);
        document.setArchivedAt(LocalDateTime.now());
        document.setArchivedBy(profile.getUser());

        documentRepository.save(document);
    }

    /**
     * Restore an archived document
     */
    @Transactional
    public Document restoreDocument(Long documentId, CorporationProfile profile) {
        Document document = getDocumentEntity(documentId, profile);

        // Check DELETE permission (same as archive)
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.DELETE);

        document.setStatus(DocumentStatus.ACTIVE);
        document.setArchivedAt(null);
        document.setArchivedBy(null);

        return documentRepository.save(document);
    }

    /**
     * Upload a new version of a document
     */
    @Transactional
    public Document uploadNewVersion(
            Long documentId,
            CorporationProfile profile,
            MultipartFile file
    ) throws IOException {

        Document currentDocument = getDocumentEntity(documentId, profile);

        // Check EDIT permission
        permissionService.checkPermission(currentDocument, profile, DocumentPermissionLevel.EDIT);

        // Validate file
        validateFile(file);

        Corporation corporation = profile.getCorporation();

        // Generate storage path
        String storagePath = UUID.randomUUID() + "/" + file.getOriginalFilename();

        // Save to storage
        StorageProvider storage = storageRouter.forCompany(corporation.getId());
        StoredFile stored = storage.save(
                corporation.getId(),
                storagePath,
                file.getInputStream(),
                file.getContentType()
        );

        // Create new version
        Document newVersion = Document.builder()
                .corporation(corporation)
                .originalFilename(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(stored.size())
                .contentType(file.getContentType())
                .description(currentDocument.getDescription())
                .version(currentDocument.getVersion() + 1)
                .previousVersion(currentDocument)
                .latestVersion(null) // Will be set to itself
                //.tags(new HashSet<>(currentDocument.getTags()))
                .uploadedBy(profile.getUser())
                .status(DocumentStatus.ACTIVE)
                .build();

        Document savedNewVersion = documentRepository.save(newVersion);

        // Update latestVersion reference
        savedNewVersion.setLatestVersion(savedNewVersion);
        currentDocument.setLatestVersion(savedNewVersion);

        documentRepository.save(savedNewVersion);
        documentRepository.save(currentDocument);

        return savedNewVersion;
    }

    /**
     * Get all versions of a document
     */
    @Transactional(readOnly = true)
    public List<Document> getVersions(Long documentId, CorporationProfile profile) {
        Document document = getDocumentEntity(documentId, profile);

        // Check READ permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.READ);

        // Get latest version
        Document latestVersion = document.getLatestVersion();

        return documentRepository.findAllVersions(latestVersion);
    }

    /**
     * Set a specific version as the latest version
     */
    @Transactional
    public Document setAsLatest(Long documentId, CorporationProfile profile) {
        Document targetVersion = getDocumentEntity(documentId, profile);

        // Check EDIT permission
        permissionService.checkPermission(targetVersion, profile, DocumentPermissionLevel.EDIT);

        // Get the current latest version
        Document currentLatest = targetVersion.getLatestVersion();

        // If this version is already the latest, do nothing
        if (targetVersion.getId().equals(currentLatest.getId())) {
            return targetVersion;
        }

        // Get all versions
        List<Document> allVersions = documentRepository.findAllVersions(currentLatest);

        // Update all versions to point to the new latest
        for (Document version : allVersions) {
            version.setLatestVersion(targetVersion);
            documentRepository.save(version);
        }

        return targetVersion;
    }

    /**
     * Search documents by filename
     */
    @Transactional(readOnly = true)
    public List<Document> searchByFilename(CorporationProfile profile, String search) {
        // For now, return all documents - can be optimized later
        return listDocuments(profile).stream()
                .filter(doc -> doc.getOriginalFilename().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Get document entity (internal use)
     */
    private Document getDocumentEntity(Long documentId, CorporationProfile profile) {
        return documentRepository.findByIdAndCorporation(documentId, profile.getCorporation())
                .orElseThrow(() -> new RuntimeException("Document not found: " + documentId));
    }

    /**
     * Get total storage used by all documents in corporation
     */
    @Transactional(readOnly = true)
    public long getTotalStorageUsed(CorporationProfile profile) {
        List<Document> documents = documentRepository.findByCorporationAndStatus(
                profile.getCorporation(),
                DocumentStatus.ACTIVE
        );

        return documents.stream()
                .mapToLong(Document::getSize)
                .sum();
    }

    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new  RuntimeException("File is too large");
            //throw new FileSizeLimitException(file.getSize(), MAX_FILE_SIZE);
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new  RuntimeException("File is invalid");
            //throw new UnsupportedFileTypeException(contentType);
        }
    }
}