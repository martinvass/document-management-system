package hu.martinvass.dms.corporation.settings.dto;

import hu.martinvass.dms.corporation.settings.StorageType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StorageSettingsDto {

    private StorageType storageType;

    private String s3Region;
    private String s3Bucket;
    private String s3Prefix;

    private String s3AccessKey;
    private String s3SecretKey;

    private boolean hasStoredCredentials;
}