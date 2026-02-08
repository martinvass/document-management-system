package hu.martinvass.dms.corporation.settings.storage.impl;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import hu.martinvass.dms.corporation.settings.storage.StorageSettingsProvider;
import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.corporation.repository.CompanyStorageRepository;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3StorageSettingsProvider implements StorageSettingsProvider {

    private final CompanyStorageRepository companyStorageRepository;

    public S3StorageSettingsProvider(CompanyStorageRepository companyStorageRepository) {
        this.companyStorageRepository = companyStorageRepository;
    }

    @Override
    public StorageType supports() {
        return StorageType.CUSTOM_S3;
    }

    @Override
    public void testConnection(Long companyId, StorageSettingsDto dto) {
        CompanySettings cfg = companyStorageRepository.findByCorporationId(companyId)
                .orElseGet(() -> defaultStorageConfig(companyId));

        String accessKey = dto.getS3AccessKey() == null ? cfg.getS3AccessKey() : dto.getS3AccessKey();
        String secretKey = dto.getS3SecretKey() == null ? cfg.getS3AccessKey() : dto.getS3SecretKey();

        try (S3Client s3 = buildClient(
                dto.getS3Region(),
                accessKey,
                secretKey
        )) {

            // Bucket available
            try {
                s3.headBucket(HeadBucketRequest.builder()
                        .bucket(dto.getS3Bucket())
                        .build());
            } catch (S3Exception e) {
                throw new RuntimeException(
                        "Bucket not accessible: " + e.awsErrorDetails().errorMessage()
                );
            }

            // Check prefix
            String testKey = normalizePrefix(dto.getS3Prefix()) + ".dms-test";

            try {
                s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(dto.getS3Bucket())
                                .key(testKey)
                                .build(),
                        RequestBody.fromBytes("test".getBytes())
                );

                s3.deleteObject(DeleteObjectRequest.builder()
                        .bucket(dto.getS3Bucket())
                        .key(testKey)
                        .build());

            } catch (S3Exception e) {
                throw new RuntimeException(
                        "No write permission for prefix: " + dto.getS3Prefix()
                );
            }
        }
    }

    @Override
    public void applySettings(Long companyId, StorageSettingsDto dto) {
        CompanySettings cfg = companyStorageRepository.findByCorporationId(companyId)
                .orElseGet(() -> defaultStorageConfig(companyId));

        cfg.setStorageType(StorageType.CUSTOM_S3);
        cfg.setS3Region(dto.getS3Region());
        cfg.setS3Bucket(dto.getS3Bucket());
        cfg.setS3Prefix(dto.getS3Prefix());

        if (dto.getS3AccessKey() != null && !dto.getS3AccessKey().isBlank()) {
            cfg.setS3AccessKey(dto.getS3AccessKey());
        }
        if (dto.getS3SecretKey() != null && !dto.getS3SecretKey().isBlank()) {
            cfg.setS3SecretKey(dto.getS3SecretKey());
        }

        companyStorageRepository.save(cfg);
    }

    @Override
    public StorageSettingsDto loadSettings(Long companyId) {
        CompanySettings cfg = companyStorageRepository.findByCorporationId(companyId)
                .orElseGet(() -> defaultStorageConfig(companyId));

        StorageSettingsDto dto = new StorageSettingsDto();
        dto.setStorageType(StorageType.CUSTOM_S3);
        dto.setS3Region(cfg.getS3Region());
        dto.setS3Bucket(cfg.getS3Bucket());
        dto.setS3Prefix(cfg.getS3Prefix());
        dto.setHasStoredCredentials(
                cfg.getS3AccessKey() != null && cfg.getS3SecretKey() != null
        );

        return dto;
    }

    private S3Client buildClient(String region, String accessKey, String secretKey) {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .build();
    }

    private String normalizePrefix(String prefix) {
        String p = prefix.trim();
        if (!p.endsWith("/")) p += "/";
        return p;
    }

    private CompanySettings defaultStorageConfig(Long companyId) {
        CompanySettings cfg = new CompanySettings();
        cfg.setCorporationId(companyId);
        cfg.setStorageType(StorageType.MANAGED);

        return cfg;
    }
}