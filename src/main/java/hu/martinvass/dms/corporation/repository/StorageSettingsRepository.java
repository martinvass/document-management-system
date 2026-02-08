package hu.martinvass.dms.corporation.repository;

import hu.martinvass.dms.corporation.domain.CompanySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StorageSettingsRepository extends JpaRepository<CompanySettings, Long> {
    Optional<CompanySettings> findByCorporationId(Long id);
}