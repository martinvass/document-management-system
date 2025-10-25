package hu.martinvass.dms.corporation.repository;

import hu.martinvass.dms.corporation.Corporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Repository
public interface CorporationRepository extends JpaRepository<Corporation, Long> {

    boolean existsByName(String name);
}