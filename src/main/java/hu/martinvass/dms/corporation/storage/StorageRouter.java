package hu.martinvass.dms.corporation.storage;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import hu.martinvass.dms.corporation.repository.CompanyStorageRepository;
import hu.martinvass.dms.corporation.settings.storage.StorageType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StorageRouter {

    private final Map<StorageType, StorageProvider> providers;
    private final CompanyStorageRepository settingsRepository;

    public StorageRouter(
            List<StorageProvider> providers,
            CompanyStorageRepository settingsRepository
    ) {
        // Spring automatikusan betölti az összes StorageProvider-t
        this.providers = providers.stream()
                .collect(Collectors.toMap(
                        StorageProvider::supports,
                        Function.identity()
                ));
        this.settingsRepository = settingsRepository;
    }

    public StorageProvider forCompany(Long companyId) {
        StorageType type = settingsRepository
                .findByCorporationId(companyId)
                .map(CompanySettings::getStorageType)
                .orElse(StorageType.MANAGED);

        StorageProvider provider = providers.get(type);

        if (provider == null) {
            throw new IllegalStateException(
                    "No StorageProvider registered for type: " + type
            );
        }

        return provider;
    }
}