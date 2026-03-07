package hu.martinvass.dms.document.service;

import hu.martinvass.dms.activity.ActivityType;
import hu.martinvass.dms.activity.service.ActivityService;
import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.document.domain.Document;
import hu.martinvass.dms.document.domain.DocumentPermissionLevel;
import hu.martinvass.dms.document.domain.DocumentStatus;
import hu.martinvass.dms.document.repository.DocumentRepository;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.storage.StorageProvider;
import hu.martinvass.dms.storage.StorageRouter;
import hu.martinvass.dms.user.domain.AppUser;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageRouter storageRouter;
    private final DocumentPermissionService permissionService;
    private final ActivityService activityService;

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
            String description
    ) throws IOException {

        // Check if user can upload
        if (!permissionService.canUploadDocuments(profile)) {
            throw new SecurityException("User does not have permission to upload documents");
        }

        // Validate file
        validateFile(file);

        var corporation = profile.getCorporation();

        // Generate storage path
        var storagePath = UUID.randomUUID() + "/" + file.getOriginalFilename();

        // Save to storage
        var storage = storageRouter.forCompany(corporation.getId());
        var stored = storage.save(
                corporation.getId(),
                storagePath,
                file.getInputStream(),
                file.getContentType()
        );

        var currentStorageType = storageRouter.getStorageType(corporation.getId());

        // Create document entity
        var document = Document.builder()
                .corporation(corporation)
                .originalFilename(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(stored.size())
                .contentType(file.getContentType())
                .description(description)
                .storageType(currentStorageType)
                .uploadedBy(profile.getUser())
                .status(DocumentStatus.ACTIVE)
                .build();

        activityService.log(
                corporation,
                profile.getUser(),
                ActivityType.DOCUMENT_UPLOADED,
                document.getOriginalFilename(),
                document.getId()
        );

        return documentRepository.save(document);
    }

    /**
     * List all active documents
     */
    @Transactional(readOnly = true)
    public List<Document> listDocuments(CorporationProfile profile) {
        Corporation corporation = profile.getCorporation();
        return documentRepository.findLatestVersionsByCorporationAndStatus(
                corporation,
                DocumentStatus.ACTIVE
        );
    }

    /**
     * Get a single document
     */
    @Transactional(readOnly = true)
    public Document getDocument(Long documentId, CorporationProfile profile) {
        var document = getDocumentEntity(documentId, profile);

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
            String description
    ) {
        var document = getDocumentEntity(documentId, profile);

        // Check EDIT permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.EDIT);

        // Update description
        if (description != null) {
            document.setDescription(description);
        }

        activityService.log(
                document.getCorporation(),
                profile.getUser(),
                ActivityType.DOCUMENT_UPDATED,
                document.getOriginalFilename(),
                document.getId()
        );

        return documentRepository.save(document);
    }

    /**
     * Download document
     */
    @Transactional
    public InputStream download(Long documentId, CorporationProfile profile) throws IOException {
        var document = getDocumentEntity(documentId, profile);

        // Check READ permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.READ);

        //StorageProvider storage = storageRouter.forCompany(document.getCorporation().getId());
        var storage = getStorageProviderForDocument(document);

        activityService.log(
                document.getCorporation(),
                profile.getUser(),
                ActivityType.DOCUMENT_DOWNLOADED,
                document.getOriginalFilename(),
                document.getId()
        );

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
        var document = getDocumentEntity(documentId, profile);

        // Check DELETE permission
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.DELETE);

        document.setStatus(DocumentStatus.ARCHIVED);
        document.setArchivedAt(LocalDateTime.now());
        document.setArchivedBy(profile.getUser());

        documentRepository.save(document);

        activityService.log(
                document.getCorporation(),
                profile.getUser(),
                ActivityType.DOCUMENT_ARCHIVED,
                document.getOriginalFilename(),
                document.getId()
        );
    }

    /**
     * Restore an archived document
     */
    @Transactional
    public Document restoreDocument(Long documentId, CorporationProfile profile) {
        var document = getDocumentEntity(documentId, profile);

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
        var currentDocument = getDocumentEntity(documentId, profile);

        // Check EDIT permission
        permissionService.checkPermission(currentDocument, profile, DocumentPermissionLevel.EDIT);

        // Validate file
        validateFile(file);

        var corporation = profile.getCorporation();

        // Generate storage path
        var storagePath = UUID.randomUUID() + "/" + file.getOriginalFilename();

        // Save to storage
        var storage = storageRouter.forCompany(corporation.getId());
        var stored = storage.save(
                corporation.getId(),
                storagePath,
                file.getInputStream(),
                file.getContentType()
        );

        var newVersion = Document.builder()
                .corporation(corporation)
                .originalFilename(file.getOriginalFilename())
                .storagePath(storagePath)
                .size(stored.size())
                .contentType(file.getContentType())
                .description(currentDocument.getDescription())
                .version(currentDocument.getVersion() + 1)
                .previousVersion(currentDocument)
                .latestVersion(null)
                .uploadedBy(profile.getUser())
                .status(DocumentStatus.ACTIVE)
                .build();

        var savedNewVersion = documentRepository.save(newVersion);

        savedNewVersion.setLatestVersion(savedNewVersion);
        currentDocument.setLatestVersion(savedNewVersion);

        permissionService.copyPermissions(currentDocument, newVersion);

        documentRepository.save(savedNewVersion);
        documentRepository.save(currentDocument);

        activityService.log(
                corporation,
                profile.getUser(),
                ActivityType.NEW_VERSION_UPLOADED,
                newVersion.getOriginalFilename(),
                newVersion.getId(),
                "Version " + newVersion.getVersion()
        );

        return savedNewVersion;
    }

    @Transactional
    public Document migrateDocument(Long documentId, CorporationProfile profile) throws IOException {
        var document = getDocumentEntity(documentId, profile);

        permissionService.checkPermission(document, profile, DocumentPermissionLevel.EDIT);

        var corporation = document.getCorporation();
        var currentStorageType = storageRouter.getStorageType(corporation.getId());

        if (document.getStorageType() == currentStorageType)
            return document;

        var oldStorage = storageRouter.getProviderByType(document.getStorageType());
        var newStorage = storageRouter.forCompany(corporation.getId());

        try {
            var oldContent = oldStorage.load(corporation.getId(), document.getStoragePath());
            var newStoragePath = UUID.randomUUID() + "/" + document.getOriginalFilename();
            var newStored = newStorage.save(
                    corporation.getId(),
                    newStoragePath,
                    oldContent,
                    document.getContentType()
            );

            var oldPath = document.getStoragePath();

            document.setStoragePath(newStoragePath);
            document.setStorageType(currentStorageType);
            document.setSize(newStored.size());
            document.setModifiedAt(LocalDateTime.now());

            var savedDocument = documentRepository.save(document);

            try {
                oldStorage.delete(corporation.getId(), oldPath);
            } catch (Exception ignored) {
            }

            return savedDocument;
        } catch (IOException e) {
            throw new IOException("Failed to migrate document: " + e.getMessage(), e);
        }
    }

    /**
     * Get all versions of a document
     */
    @Transactional(readOnly = true)
    public List<Document> getVersions(Long documentId, CorporationProfile profile) {
        var document = getDocumentEntity(documentId, profile);
        permissionService.checkPermission(document, profile, DocumentPermissionLevel.READ);

        var latestVersion = document.getLatestVersion();
        return documentRepository.findAllVersions(latestVersion);
    }

    /**
     * Set a specific version as the latest version
     */
    @Transactional
    public Document setAsLatest(Long documentId, CorporationProfile profile) {
        var targetVersion = getDocumentEntity(documentId, profile);
        permissionService.checkPermission(targetVersion, profile, DocumentPermissionLevel.EDIT);

        var currentLatest = targetVersion.getLatestVersion();

        if (targetVersion.getId().equals(currentLatest.getId())) {
            return targetVersion;
        }

        var allVersions = documentRepository.findAllVersions(currentLatest);

        for (var version : allVersions) {
            version.setLatestVersion(targetVersion);
            documentRepository.save(version);
        }

        return targetVersion;
    }

    /**
     * Find documents with filters
     *
     * @param corporation Corporation to filter by
     * @param currentUser Current logged-in user
     * @param search Search term for filename
     * @param departmentId Department filter
     * @param fileType File type filter (pdf, docx, xlsx, image)
     * @param myUploads Filter only documents uploaded by current user
     * @param pageable Pagination
     * @return Filtered documents
     */
    public Page<Document> findFiltered(
            Corporation corporation,
            AppUser currentUser,
            String search,
            Long departmentId,
            String fileType,
            Boolean myUploads,
            Pageable pageable
    ) {
        Specification<Document> spec = (root, query, cb) -> {
            var predicates = new ArrayList<>();

            // 1. Corporation filter (ALWAYS required)
            predicates.add(cb.equal(root.get("corporation"), corporation));

            // 2. Status filter (ACTIVE only)
            predicates.add(cb.equal(root.get("status"), DocumentStatus.ACTIVE));

            // 3. Latest version only
            predicates.add(cb.equal(root.get("latestVersion"), root));

            // 4. Search by filename (optional)
            if (search != null && !search.trim().isEmpty()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("originalFilename")),
                                "%" + search.toLowerCase().trim() + "%"
                        )
                );
            }

            // 5. Department filter (optional)
            if (departmentId != null) {
                var departmentsJoin = root.join("departments", JoinType.INNER);
                predicates.add(cb.equal(departmentsJoin.get("id"), departmentId));
            }

            // 6. File type filter (optional)
            if (fileType != null && !fileType.trim().isEmpty()) {
                var type = fileType.toLowerCase().trim();
                switch (type) {
                    case "pdf":
                        predicates.add(cb.equal(root.get("contentType"), "application/pdf"));
                        break;
                    case "docx":
                        predicates.add(cb.like(root.get("contentType"), "%wordprocessingml%"));
                        break;
                    case "xlsx":
                        predicates.add(cb.like(root.get("contentType"), "%spreadsheetml%"));
                        break;
                    case "image":
                        predicates.add(cb.like(root.get("contentType"), "image/%"));
                        break;
                }
            }

            // 7. My Uploads filter (optional)
            if (Boolean.TRUE.equals(myUploads)) {
                predicates.add(cb.equal(root.get("uploadedBy"), currentUser));
            }

            // Combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return documentRepository.findAll(spec, pageable);
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
        var documents = documentRepository.findByCorporationAndStatus(
                profile.getCorporation(),
                DocumentStatus.ACTIVE
        );

        return documents.stream()
                .mapToLong(Document::getSize)
                .sum();
    }

    /**
     * Get total number of active documents in corporation
     */
    @Transactional(readOnly = true)
    public long getTotalDocuments(CorporationProfile profile) {
        return documentRepository.countByCorporationAndStatus(
                profile.getCorporation(),
                DocumentStatus.ACTIVE
        );
    }

    /**
     * Get number of documents that need migration
     * (documents where storageType != current company storage type)
     */
    @Transactional(readOnly = true)
    public long getDocumentsToMigrate(CorporationProfile profile) {
        var corporation = profile.getCorporation();
        var currentStorageType = storageRouter.getStorageType(corporation.getId());

        var allDocs = documentRepository.findByCorporationAndStatus(
                corporation,
                DocumentStatus.ACTIVE
        );

        return allDocs.stream()
                .filter(doc -> doc.getStorageType() != null && doc.getStorageType() != currentStorageType)
                .count();
    }

    /**
     * Get recent documents (latest uploads)
     */
    @Transactional(readOnly = true)
    public List<Document> getRecentDocuments(CorporationProfile profile, int limit) {
        var allDocs = documentRepository.findLatestVersionsByCorporationAndStatus(
                profile.getCorporation(),
                DocumentStatus.ACTIVE
        );

        return allDocs.stream()
                .limit(limit)
                .toList();
    }

    /**
     * Get user's recent uploads
     */
    @Transactional(readOnly = true)
    public List<Document> getUserRecentDocuments(AppUser user, int limit) {
        return documentRepository.findByUploadedByOrderByUploadedAtDesc(user).stream()
                .limit(limit)
                .toList();
    }

    private StorageProvider getStorageProviderForDocument(Document document) {
        return storageRouter.getProviderByType(document.getStorageType());
    }

    public StorageType getCurrentStorageType(Long corporationId) {
        return storageRouter.getStorageType(corporationId);
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