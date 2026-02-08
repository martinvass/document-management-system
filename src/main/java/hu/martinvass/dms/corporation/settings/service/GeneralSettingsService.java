package hu.martinvass.dms.corporation.settings.service;

import hu.martinvass.dms.corporation.settings.dto.GeneralSettingsDto;

public interface GeneralSettingsService {
    GeneralSettingsDto get(Long p0);
    void save(Long p0, GeneralSettingsDto p1);
}