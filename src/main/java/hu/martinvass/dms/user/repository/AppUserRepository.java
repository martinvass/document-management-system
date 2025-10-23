package hu.martinvass.dms.user.repository;

import hu.martinvass.dms.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link hu.martinvass.dms.user.AppUser} entities.
 * Provides methods to interact with the underlying data store.
 */
@Repository
@Transactional(readOnly = true)
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByProfile_Email(String email);
    Optional<AppUser> findByProfile_Username(String username);

    boolean existsByProfile_Email(String email);
}