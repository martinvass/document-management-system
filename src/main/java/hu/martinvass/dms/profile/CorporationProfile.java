package hu.martinvass.dms.profile;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.department.domain.Department;
import hu.martinvass.dms.user.domain.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "company_membership")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CorporationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "company_membership_seq")
    @SequenceGenerator(name = "company_membership_seq", sequenceName = "company_membership_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne()
    @JoinColumn(name = "corporation_id")
    private Corporation corporation;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private CorporationRole role;

    @ManyToMany
    @JoinTable(
            name = "profile_departments",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id")
    )
    private Set<Department> departments = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public boolean isCorporationAdmin() {
        return role == CorporationRole.ADMIN;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}