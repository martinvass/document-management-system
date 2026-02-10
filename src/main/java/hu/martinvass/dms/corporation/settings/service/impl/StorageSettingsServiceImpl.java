package hu.martinvass.dms.corporation.settings.service.impl;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import hu.martinvass.dms.corporation.settings.storage.StorageSettingsProvider;
import hu.martinvass.dms.corporation.settings.service.StorageSettingsService;
import hu.martinvass.dms.corporation.settings.storage.StorageType;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.corporation.repository.CompanyStorageRepository;
import hu.martinvass.dms.corporation.settings.storage.impl.ManagedStorageSettingsProvider;
import hu.martinvass.dms.corporation.settings.storage.impl.AwsStorageSettingsProvider;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StorageSettingsServiceImpl implements StorageSettingsService {

    private final Map<StorageType, StorageSettingsProvider> providers;
    private final CompanyStorageRepository companyStorageRepository;

    public StorageSettingsServiceImpl(CompanyStorageRepository companyStorageRepository) {
        this.providers = Map.of(
                StorageType.MANAGED, new ManagedStorageSettingsProvider(),
                StorageType.CUSTOM_S3, new AwsStorageSettingsProvider(companyStorageRepository)
        );
        this.companyStorageRepository = companyStorageRepository;
    }

    @Override
    public StorageSettingsDto get(Long companyId) {
        CompanySettings cfg = companyStorageRepository.findByCorporationId(companyId)
                .orElseGet(() -> defaultStorageConfig(companyId));

        return providers.get(cfg.getStorageType()).loadSettings(companyId);
    }

    @Override
    public void save(Long companyId, StorageSettingsDto dto) {
        providers.get(dto.getStorageType())
                .applySettings(companyId, dto);
    }

    @Override
    public void testConnection(Long companyId, StorageSettingsDto dto) {
        providers.get(dto.getStorageType())
                .testConnection(companyId, dto);
    }

    private CompanySettings defaultStorageConfig(Long companyId) {
        CompanySettings cfg = new CompanySettings();
        cfg.setCorporationId(companyId);
        cfg.setStorageType(StorageType.MANAGED);

        return cfg;
    }
}