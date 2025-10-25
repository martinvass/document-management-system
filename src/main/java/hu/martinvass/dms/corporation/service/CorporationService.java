package hu.martinvass.dms.corporation.service;

import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.corporation.CorporationRole;
import hu.martinvass.dms.corporation.data.CreateCorporationDTO;
import hu.martinvass.dms.corporation.event.CorporationCreatedEvent;
import hu.martinvass.dms.corporation.repository.CorporationRepository;
import hu.martinvass.dms.user.repository.AppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CorporationService {

    private final CorporationRepository corporationRepository;
    private final AppUserRepository appUserRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void createCorporation(CreateCorporationDTO dto, String user) {
        if (corporationRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Corporation with name " + dto.getName() + " already exists");
        }

        var creatorUser = appUserRepository.findByProfile_Username(user)
                .orElseThrow(() -> new UsernameNotFoundException("User " + user + " not found"));

        // Create corporation
        var corp = Corporation.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .owner(creatorUser)
                .build();

        corporationRepository.save(corp);

        // Assign corporation to the user
        creatorUser.setCorporation(corp);
        creatorUser.setCorporationRole(CorporationRole.ADMIN);
        appUserRepository.save(creatorUser);

        applicationEventPublisher.publishEvent(new CorporationCreatedEvent(creatorUser, corp));
    }
}