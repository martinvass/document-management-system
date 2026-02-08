package hu.martinvass.dms.corporation.document.repository;

import hu.martinvass.dms.corporation.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository
        extends JpaRepository<Document, Long> {

    List<Document> findByCorporationIdOrderByUploadedAtDesc(Long corporationId);
}