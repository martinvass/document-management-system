package hu.martinvass.dms.user.repository;

import hu.martinvass.dms.user.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Repository interface for accessing and managing {@link AppUser} entities.
 * Provides methods to interact with the underlying data store.
 */
@Repository
@Transactional(readOnly = true)
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByUsername(String username);

    boolean existsByEmail(String email);
}