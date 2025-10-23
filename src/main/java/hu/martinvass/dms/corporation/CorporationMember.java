package hu.martinvass.dms.corporation;

import hu.martinvass.dms.user.Profile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_id", "corp_id"})
})
@Getter
@Setter
public class CorporationMember {

    @SequenceGenerator(
            name = "corp_m_sequence",
            sequenceName = "corp_m_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "corp_m_sequence"
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corp_id", nullable = false)
    private Corporation corporation;

    @Enumerated(EnumType.STRING)
    private CorporationRole role;

    @Column(name = "joined_at", nullable = false)
    private Date joinedAt = new Date(System.currentTimeMillis());
}