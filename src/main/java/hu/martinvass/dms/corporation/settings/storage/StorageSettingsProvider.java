package hu.martinvass.dms.corporation.settings.storage;

import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;

public interface StorageSettingsProvider {
    StorageType supports();

    void testConnection(Long p0, StorageSettingsDto p1);

    void applySettings(Long p0, StorageSettingsDto p1);

    StorageSettingsDto loadSettings(Long p0);
}