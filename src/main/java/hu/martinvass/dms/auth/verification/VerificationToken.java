package hu.martinvass.dms.auth.verification;

import hu.martinvass.dms.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @SequenceGenerator(
            name = "verification_sequence",
            sequenceName = "verification_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "verification_sequence"
    )
    private Long id;

    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id")
    private AppUser user;

    private Date expiryDate;

    public VerificationToken(AppUser user) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiryDate = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
    }
}