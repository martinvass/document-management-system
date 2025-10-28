package hu.martinvass.dms.invitation.service;

import hu.martinvass.dms.events.InvitationCreatedEvent;
import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.invitation.InvitationStatus;
import hu.martinvass.dms.data.CreateInvitationDTO;
import hu.martinvass.dms.data.InvitationStatsDTO;
import hu.martinvass.dms.invitation.repository.InvitationRepository;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import hu.martinvass.dms.user.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final CorporationProfileRepository profilesRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createInvitation(CreateInvitationDTO data, CorporationProfile profile) {
        var invitation = Invitation.fromDto(data, profile);

        invitationRepository.save(invitation);

        eventPublisher.publishEvent(new InvitationCreatedEvent(invitation, profile.getUser()));
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
    public Page<Invitation> findByCorporation(Long corporationId, Pageable pageable) {
        return invitationRepository.findByCorporationId(corporationId, pageable);
    }

    @Transactional(readOnly = true)
    public Invitation findByCode(String code) {
        return invitationRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
    }

    @Transactional(readOnly = true)
    public InvitationStatsDTO getStatsForCorporation(Long corporationId) {
        int pending = Math.toIntExact(invitationRepository.countByCorporationIdAndStatus(corporationId, InvitationStatus.PENDING));
        int accepted = Math.toIntExact(invitationRepository.countByCorporationIdAndStatus(corporationId, InvitationStatus.ACCEPTED));
        int revoked = Math.toIntExact(invitationRepository.countByCorporationIdAndStatus(corporationId, InvitationStatus.REVOKED));

        return new InvitationStatsDTO(pending, accepted, revoked);
    }

    @Transactional
    public void markAsAccepted(Invitation invitation) {
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(new Date(System.currentTimeMillis()));

        invitationRepository.save(invitation);
    }

    public boolean isExpired(Invitation invitation) {
        return invitation.getExpiresAt() != null && invitation.getExpiresAt().before(new Date());
    }

    @Transactional
    public void acceptInvitation(String code, AppUser user) {
        Invitation invitation = findByCode(code);

        // Validation
        if (!invitation.getInvitedEmail().equalsIgnoreCase(user.getProfile().getEmail())) {
            throw new SecurityException("This invitation is not addressed to your email address.");
        }

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is already used");
        }

        if (isExpired(invitation)) {
            throw new IllegalStateException("Invitation is expired");
        }

        // Create corporation profile
        var profile = new CorporationProfile();
        profile.setUser(user);
        profile.setProfile(user.getProfile());
        profile.setCorporation(invitation.getCorporation());
        profile.setRole(invitation.getRole());
        profilesRepository.save(profile);

        // Mark invitation as accepted
        markAsAccepted(invitation);
    }
}