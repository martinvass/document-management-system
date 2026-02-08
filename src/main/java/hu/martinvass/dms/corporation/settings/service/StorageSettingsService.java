package hu.martinvass.dms.corporation.settings.service;

import hu.martinvass.dms.corporation.settings.dto.StorageSettingsDto;

public interface StorageSettingsService {

    StorageSettingsDto get(Long p0);

    void save(Long p0, StorageSettingsDto p1);

    void testConnection(Long p0, StorageSettingsDto p1);
}