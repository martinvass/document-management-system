package hu.martinvass.dms.audit;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import java.util.Date;

@Entity(name = "audit_logs")
@AllArgsConstructor
public class AuditLogEntry {

    @SequenceGenerator(
            name = "audit_sequence",
            sequenceName = "audit_sequence",
            allocationSize = 1
    )
    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "audit_sequence"
    )
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditEventAction action;
    private Long userId;

    @Column(length = 2000)
    private String details;

    private Date timestamp = new Date(System.currentTimeMillis());

    public AuditLogEntry() {}

    public AuditLogEntry(AuditEventAction action, Long userId, String details) {
        this.action = action;
        this.userId = userId;
        this.details = details;
    }
}