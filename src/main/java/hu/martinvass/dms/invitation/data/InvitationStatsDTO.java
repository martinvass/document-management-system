package hu.martinvass.dms.invitation.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitationStatsDTO {

    private int pendingCount;
    private int acceptedCount;
    private long revokedCount;
}