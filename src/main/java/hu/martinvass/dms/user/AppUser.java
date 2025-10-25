package hu.martinvass.dms.user;

import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.corporation.CorporationRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * Class representing an application user implementing Spring Security's UserDetails interface.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@Entity(name = "users")
public class AppUser implements UserDetails {

    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    private Long id;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile = new Profile();

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corp_id")
    private Corporation corporation;

    @Enumerated(EnumType.STRING)
    private CorporationRole corporationRole;

    @Enumerated(EnumType.STRING)
    private GlobalRole role = GlobalRole.NO_ROLE;

    @Column(nullable = false)
    private boolean verified = false;

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Date creationDate = new Date(System.currentTimeMillis());

    public AppUser() {}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role.name());

        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return profile.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !(status == AccountStatus.SUSPENDED || status == AccountStatus.INACTIVE);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == AccountStatus.ACTIVE && verified;
    }

    public boolean isInCorporation() {
        return this.corporation != null;
    }

    public boolean isCorporationAdmin() {
        return this.corporation != null
                && this.corporationRole == CorporationRole.ADMIN;
    }

    public boolean isSystemAdmin() {
        return this.role == GlobalRole.SYSTEM_ADMIN;
    }
}