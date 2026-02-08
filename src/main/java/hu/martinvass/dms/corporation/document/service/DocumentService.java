package hu.martinvass.dms.corporation.document.service;

import hu.martinvass.dms.corporation.document.domain.Document;
import hu.martinvass.dms.corporation.document.repository.DocumentRepository;
import hu.martinvass.dms.corporation.storage.StorageProvider;
import hu.martinvass.dms.corporation.storage.StorageRouter;
import hu.martinvass.dms.corporation.storage.StoredFile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageRouter storageRouter;

    @Transactional
    public void upload(
            Long corporationId,
            Long userId,
            MultipartFile file
    ) throws IOException {

        String storagePath =
                UUID.randomUUID() + "/" + file.getOriginalFilename();

        StorageProvider storage =
                storageRouter.forCompany(corporationId);

        StoredFile stored = storage.save(
                corporationId,
                storagePath,
                file.getInputStream(),
                file.getContentType()
        );

        Document doc = new Document(
                corporationId,
                file.getOriginalFilename(),
                storagePath,
                stored.size(),
                file.getContentType(),
                userId
        );

        documentRepository.save(doc);
    }

    @Transactional(readOnly = true)
    public List<Document> list(Long corporationId) {
        return documentRepository
                .findByCorporationIdOrderByUploadedAtDesc(corporationId);
    }

    @Transactional(readOnly = true)
    public InputStream download(Long corporationId, Long documentId) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Document not found"));

        if (!doc.getCorporationId().equals(corporationId)) {
            throw new SecurityException("Access denied");
        }

        StorageProvider storage =
                storageRouter.forCompany(corporationId);

        return storage.load(
                corporationId,
                doc.getStoragePath()
        );
    }

    @Transactional(readOnly = true)
    public Document getDocument(Long corporationId, Long documentId) throws IOException {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("Document not found"));

        if (!doc.getCorporationId().equals(corporationId)) {
            throw new SecurityException("Access denied");
        }

        return doc;
    }
}