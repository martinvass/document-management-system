package hu.martinvass.dms.invitation;

import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.corporation.CorporationRole;
import hu.martinvass.dms.user.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

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
}