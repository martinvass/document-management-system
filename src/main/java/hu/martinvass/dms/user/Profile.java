package hu.martinvass.dms.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Class representing all informational data about an application user.
 */
@Getter
@Setter
@Builder
@Entity(name = "profiles")
@AllArgsConstructor
public class Profile {

    @SequenceGenerator(
            name = "profile_sequence",
            sequenceName = "profile_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "profile_sequence"
    )
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    public  Profile() {}
}