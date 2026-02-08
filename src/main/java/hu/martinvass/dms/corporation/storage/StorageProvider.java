package hu.martinvass.dms.corporation.storage;

import hu.martinvass.dms.corporation.settings.StorageType;
import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;

public interface StorageProvider {
    StorageType supports();

    void testConnection(Long p0, StorageSettingsDto p1);

    void applySettings(Long p0, StorageSettingsDto p1);

    StorageSettingsDto loadSettings(Long p0);
}