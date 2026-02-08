package hu.martinvass.dms.corporation.settings.storage.impl;

import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;
import hu.martinvass.dms.corporation.settings.storage.StorageSettingsProvider;
import hu.martinvass.dms.corporation.settings.storage.StorageType;

public class ManagedStorageSettingsProvider implements StorageSettingsProvider {

    @Override
    public StorageType supports() {
        return StorageType.MANAGED;
    }

    @Override
    public void testConnection(Long p0, StorageSettingsDto p1) {

    }

    @Override
    public void applySettings(Long p0, StorageSettingsDto p1) {

    }

    @Override
    public StorageSettingsDto loadSettings(Long p0) {
        return StorageSettingsDto.managed();
    }
}