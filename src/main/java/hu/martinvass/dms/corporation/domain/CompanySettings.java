package hu.martinvass.dms.corporation.domain;

import hu.martinvass.dms.corporation.settings.StorageType;
import hu.martinvass.dms.corporation.settings.encryption.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "company_settings",
        uniqueConstraints = @UniqueConstraint(name = "uk_company_settings", columnNames = {"corporation_id"})
)
@Getter
@Setter
public class CompanySettings {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StorageType storageType = StorageType.MANAGED;

    // S3 fields (only for CUSTOM_S3)
    @Column(name = "s3_bucket")
    private String s3Bucket;

    @Column(name = "s3_region")
    private String s3Region;

    @Column(name = "s3_prefix")
    private String s3Prefix;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "s3_access_key_enc", length = 4096)
    private String s3AccessKey;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "s3_secret_key_enc", length = 4096)
    private String s3SecretKey;
}