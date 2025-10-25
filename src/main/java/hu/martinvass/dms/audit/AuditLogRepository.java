package hu.martinvass.dms.audit;

import hu.martinvass.dms.corporation.Corporation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    Optional<List<AuditLogEntry>> findAllByOrderByTimestampDesc();

    Optional<List<AuditLogEntry>> findByCorporationOrderByTimestampDesc(Corporation corporation);

    Optional<List<AuditLogEntry>> findByScopeOrderByTimestampDesc(AuditScope scope);
}