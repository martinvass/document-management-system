package hu.martinvass.dms.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitationStatsDTO {

    private int pendingCount;
    private int acceptedCount;
    private long revokedCount;
}