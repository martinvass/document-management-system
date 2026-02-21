package hu.martinvass.dms.corporation.storage.impl;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import hu.martinvass.dms.corporation.repository.CompanyStorageRepository;
import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.corporation.storage.StorageProvider;
import hu.martinvass.dms.corporation.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class AwsStorageProvider implements StorageProvider {

    private static final SdkHttpClient httpClient =
            UrlConnectionHttpClient.builder().build();

    private final CompanyStorageRepository companyStorageRepository;

    @Override
    public StorageType supports() {
        return StorageType.CUSTOM_S3;
    }

    @Override
    public StoredFile save(Long companyId, String logicalPath, InputStream content, String contentType) throws IOException {
        CompanySettings settings = getSettings(companyId);
        validateSettings(settings);

        String s3Key = buildS3Key(settings.getS3Prefix(), logicalPath);

        try (S3Client s3Client = buildS3Client(settings)) {
            byte[] contentBytes = content.readAllBytes();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(settings.getS3Bucket())
                    .key(s3Key)
                    .contentType(contentType)
                    .contentLength((long) contentBytes.length)
                    .build();

            s3Client.putObject(
                    putRequest,
                    RequestBody.fromBytes(contentBytes)
            );

            return new StoredFile(s3Key, contentBytes.length);

        } catch (S3Exception e) {
            throw new IOException("Failed to upload file to S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public InputStream load(Long companyId, String logicalPath) throws IOException {
        CompanySettings settings = getSettings(companyId);
        validateSettings(settings);

        String s3Key = buildS3Key(settings.getS3Prefix(), logicalPath);

        try (S3Client s3Client = buildS3Client(settings)) {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(settings.getS3Bucket())
                    .key(s3Key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getRequest);

            byte[] content = s3Object.readAllBytes();

            return new ByteArrayInputStream(content);
        } catch (NoSuchKeyException e) {
            throw new IOException("File not found in S3: " + s3Key, e);
        } catch (S3Exception e) {
            throw new IOException("Failed to download file from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    @Override
    public void delete(Long companyId, String logicalPath) throws IOException {
        CompanySettings settings = getSettings(companyId);
        validateSettings(settings);

        String s3Key = buildS3Key(settings.getS3Prefix(), logicalPath);

        try (S3Client s3Client = buildS3Client(settings)) {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(settings.getS3Bucket())
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            throw new IOException("Failed to delete file from S3: " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    /**
     * Get company storage settings
     */
    private CompanySettings getSettings(Long companyId) {
        return companyStorageRepository.findByCorporationId(companyId)
                .orElseThrow(() -> new IllegalStateException(
                        "No storage settings found for company: " + companyId
                ));
    }

    /**
     * Validate that all required S3 settings are configured
     */
    private void validateSettings(CompanySettings settings) {
        if (settings.getS3Bucket() == null || settings.getS3Bucket().isBlank()) {
            throw new IllegalStateException("S3 bucket is not configured");
        }

        if (settings.getS3Region() == null || settings.getS3Region().isBlank()) {
            throw new IllegalStateException("S3 region is not configured");
        }

        if (settings.getS3AccessKey() == null || settings.getS3AccessKey().isBlank()) {
            throw new IllegalStateException("S3 access key is not configured");
        }

        if (settings.getS3SecretKey() == null || settings.getS3SecretKey().isBlank()) {
            throw new IllegalStateException("S3 secret key is not configured");
        }
    }

    /**
     * Build S3 client with company-specific credentials
     */
    private S3Client buildS3Client(CompanySettings settings) {
        return S3Client.builder()
                .region(Region.of(settings.getS3Region()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        settings.getS3AccessKey(),
                                        settings.getS3SecretKey()
                                )
                        )
                )
                .httpClient(httpClient)
                .build();
    }

    /**
     * Build the full S3 key from prefix and logical path
     */
    private String buildS3Key(String prefix, String logicalPath) {
        String normalizedPrefix = normalizePrefix(prefix);
        String normalizedPath = logicalPath.startsWith("/")
                ? logicalPath.substring(1)
                : logicalPath;

        return normalizedPrefix + normalizedPath;
    }

    /**
     * Normalize S3 prefix (ensure it ends with /)
     */
    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "";
        }

        String normalized = prefix.trim();
        if (!normalized.endsWith("/")) {
            normalized += "/";
        }

        return normalized;
    }
}