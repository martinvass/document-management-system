package hu.martinvass.dms.audit.repository;

import hu.martinvass.dms.audit.AuditLogEntry;
import hu.martinvass.dms.audit.AuditScope;
import hu.martinvass.dms.corporation.Corporation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    Optional<List<AuditLogEntry>> findAllByOrderByTimestampDesc();

    Optional<List<AuditLogEntry>> findByCorporationOrderByTimestampDesc(Corporation corporation);

    Optional<List<AuditLogEntry>> findByScopeOrderByTimestampDesc(AuditScope scope);
}