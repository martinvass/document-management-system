package hu.martinvass.dms.invitation;

import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.corporation.CorporationRole;
import hu.martinvass.dms.data.CreateInvitationDTO;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.user.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity(name = "invitations")
@Getter
@Setter
@AllArgsConstructor
public class Invitation {

    @SequenceGenerator(
            name = "inv_sequence",
            sequenceName = "inv_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "inv_sequence"
    )
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "corp_id", nullable = false)
    private Corporation corporation;

    @Column(name = "invited")
    private String invitedEmail;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private Profile invitedBy;

    @Enumerated(EnumType.STRING)
    private CorporationRole role;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private InvitationStatus status;

    @Column(name = "expires_at")
    private Date expiresAt;

    @Column(name = "accepted_at")
    private Date acceptedAt;

    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis());

    public Invitation() {}

    public static Invitation fromDto(CreateInvitationDTO data, CorporationProfile profile) {
        var invitation = new Invitation();

        invitation.setCorporation(profile.getCorporation());
        invitation.setInvitedEmail(data.getEmail());
        invitation.setInvitedBy(profile.getProfile());
        invitation.setRole(data.getRole());
        invitation.setCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setExpiresAt(data.getExpiresAt());

        return invitation;
    }
}