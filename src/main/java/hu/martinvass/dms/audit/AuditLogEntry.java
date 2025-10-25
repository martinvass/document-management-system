package hu.martinvass.dms.audit;

import hu.martinvass.dms.corporation.Corporation;
import hu.martinvass.dms.user.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity(name = "audit_logs")
@AllArgsConstructor
@Setter
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
    @Column(nullable = false, length = 20)
    private AuditScope scope;

    @Enumerated(EnumType.STRING)
    private AuditEventAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "corp_id")
    private Corporation corporation;

    @Column(length = 2000)
    private String details;

    private Date timestamp = new Date(System.currentTimeMillis());

    public AuditLogEntry() {}

    public AuditLogEntry(AuditEventAction action, AppUser user, String details) {
        this.action = action;
        this.user = user;
        this.details = details;
    }
}