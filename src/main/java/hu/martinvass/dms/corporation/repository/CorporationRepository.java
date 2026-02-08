package hu.martinvass.dms.corporation.repository;

import hu.martinvass.dms.corporation.domain.Corporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface CorporationRepository extends JpaRepository<Corporation, Long> {

    Optional<Corporation> findByName(String name);
    boolean existsByName(String name);
}