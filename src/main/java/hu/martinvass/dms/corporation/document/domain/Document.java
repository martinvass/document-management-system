package hu.martinvass.dms.corporation.document.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long corporationId;

    private String originalFilename;

    private String storagePath;   // pl: uuid/original.pdf

    private long size;

    private String contentType;

    private LocalDateTime uploadedAt;

    private Long uploadedByUserId;

    protected Document() {}

    public Document(
            Long corporationId,
            String originalFilename,
            String storagePath,
            long size,
            String contentType,
            Long uploadedByUserId
    ) {
        this.corporationId = corporationId;
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.size = size;
        this.contentType = contentType;
        this.uploadedByUserId = uploadedByUserId;
        this.uploadedAt = LocalDateTime.now();
    }
}
