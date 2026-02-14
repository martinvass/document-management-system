package hu.martinvass.dms.corporation.settings.storage.impl;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import hu.martinvass.dms.corporation.repository.CompanyStorageRepository;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.corporation.settings.storage.StorageSettingsProvider;
import hu.martinvass.dms.corporation.settings.storage.StorageType;

public record ManagedStorageSettingsProvider(CompanyStorageRepository companyStorageRepository) implements StorageSettingsProvider {

    @Override
    public StorageType supports() {
        return StorageType.MANAGED;
    }

    @Override
    public void testConnection(Long p0, StorageSettingsDto p1) {

    }

    @Override
    public void applySettings(Long companyId, StorageSettingsDto dto) {
        CompanySettings cfg = companyStorageRepository.findByCorporationId(companyId)
                .orElseGet(() -> defaultStorageConfig(companyId));

        cfg.setStorageType(StorageType.MANAGED);
        cfg.setS3Region(null);
        cfg.setS3Bucket(null);
        cfg.setS3Prefix(null);
        cfg.setS3AccessKey(null);
        cfg.setS3SecretKey(null);

        companyStorageRepository.save(cfg);
    }

    @Override
    public StorageSettingsDto loadSettings(Long p0) {
        return StorageSettingsDto.managed();
    }

    private CompanySettings defaultStorageConfig(Long companyId) {
        CompanySettings cfg = new CompanySettings();
        cfg.setCorporationId(companyId);
        cfg.setStorageType(StorageType.MANAGED);

        return cfg;
    }
}