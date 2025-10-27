package hu.martinvass.dms.invitation.service;

import hu.martinvass.dms.events.InvitationCreatedEvent;
import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.invitation.InvitationStatus;
import hu.martinvass.dms.invitation.data.CreateInvitationDTO;
import hu.martinvass.dms.invitation.data.InvitationStatsDTO;
import hu.martinvass.dms.invitation.repository.InvitationRepository;
import hu.martinvass.dms.profile.CorporationProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createInvitation(CreateInvitationDTO data, CorporationProfile profile) {
        var invitation = new Invitation();

        // TODO: make a wrapper method that makes an invitation object based on the DTO in one line to make the code look better
        invitation.setCorporation(profile.getCorporation());
        invitation.setInvitedEmail(data.getEmail());
        invitation.setInvitedBy(profile.getProfile());
        invitation.setRole(data.getRole());
        invitation.setCode("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(data.getExpiresAt());

        var saved =  invitationRepository.save(invitation);

        eventPublisher.publishEvent(new InvitationCreatedEvent(saved, profile.getUser()));
    }

    @Transactional
    public void revokeInvitation(Long id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public List<Invitation> findByCorporation(Long corporationId) {
        return invitationRepository.findByCorporationId(corporationId);
    }

    @Transactional(readOnly = true)
    public InvitationStatsDTO getStatsForCorporation(Long corporationId) {
        int pending = Math.toIntExact(invitationRepository.countByCorporationIdAndStatus(corporationId, InvitationStatus.PENDING));
        int accepted = Math.toIntExact(invitationRepository.countByCorporationIdAndStatus(corporationId, InvitationStatus.ACCEPTED));
        int revoked = Math.toIntExact(invitationRepository.countByCorporationIdAndStatus(corporationId, InvitationStatus.REVOKED));

        return new InvitationStatsDTO(pending, accepted, revoked);
    }
}