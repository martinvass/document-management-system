package hu.martinvass.dms.invitation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InvitationStatsDto {

    private int pendingCount;
    private int acceptedCount;
    private long revokedCount;
}