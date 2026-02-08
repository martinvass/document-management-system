package hu.martinvass.dms.corporation.settings.service.impl;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.repository.CorporationRepository;
import hu.martinvass.dms.corporation.settings.service.GeneralSettingsService;
import hu.martinvass.dms.corporation.settings.dto.GeneralSettingsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GeneralSettingsServiceImpl implements GeneralSettingsService {

    private final CorporationRepository corporationRepository;

    public GeneralSettingsServiceImpl(CorporationRepository corporationRepository) {
        this.corporationRepository = corporationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public GeneralSettingsDto get(Long companyId) {
        Corporation company = corporationRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found"));

        return new GeneralSettingsDto(
                company.getName(),
                company.getDescription()
        );
    }

    @Override
    @Transactional
    public void save(Long companyId, GeneralSettingsDto dto) {
        Corporation company = corporationRepository.findById(companyId)
                .orElseThrow(() -> new IllegalStateException("Company not found"));

        company.setName(dto.getName());
        company.setDescription(dto.getDescription());

        corporationRepository.save(company);
    }
}