package hu.martinvass.dms.corporation.settings.service.impl;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import hu.martinvass.dms.corporation.storage.StorageProvider;
import hu.martinvass.dms.corporation.settings.service.StorageSettingsService;
import hu.martinvass.dms.corporation.settings.StorageType;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.corporation.repository.StorageSettingsRepository;
import hu.martinvass.dms.corporation.storage.impl.S3StorageProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StorageSettingsServiceImpl implements StorageSettingsService {

    private final Map<StorageType, StorageProvider> providers;
    private final StorageSettingsRepository storageSettingsRepository;

    public StorageSettingsServiceImpl(StorageSettingsRepository storageSettingsRepository) {
        this.providers = new HashMap<>();
        this.storageSettingsRepository = storageSettingsRepository;

        this.providers.put(StorageType.CUSTOM_S3, new S3StorageProvider(storageSettingsRepository));
        //this.providers.put(StorageType.MANAGED, new S3StorageProvider(storageRepository));
    }

    @Override
    public StorageSettingsDto get(Long companyId) {
        CompanySettings cfg = storageSettingsRepository.findByCorporationId(companyId)
                .orElseThrow();

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
}