package hu.martinvass.dms.profile;

import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.corporation.CorporationRole;
import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.user.Profile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_corp_profiles")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorporationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_profile_seq")
    @SequenceGenerator(name = "user_profile_seq", sequenceName = "user_profile_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne()
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne()
    @JoinColumn(name = "corporation_id")
    private Corporation corporation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_in_corp")
    private CorporationRole role;

    public boolean isCorporationAdmin() {
        return role == CorporationRole.ADMIN;
    }
}