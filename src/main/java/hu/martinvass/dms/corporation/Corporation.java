package hu.martinvass.dms.corporation;

import hu.martinvass.dms.user.Profile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Entity(name = "corporations")
public class Corporation {

    @SequenceGenerator(
            name = "corporation_sequence",
            sequenceName = "corporation_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "corporation_sequence"
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column()
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Profile createdBy;

    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date(System.currentTimeMillis());

    @OneToMany(mappedBy = "corporation")
    private Set<CorporationMember> members;

    public Corporation() {}
}