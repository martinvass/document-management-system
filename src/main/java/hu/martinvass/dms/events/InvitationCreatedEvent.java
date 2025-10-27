package hu.martinvass.dms.events;

import hu.martinvass.dms.invitation.Invitation;
import hu.martinvass.dms.user.AppUser;
import lombok.Getter;

import java.util.Date;

@Getter
public class InvitationCreatedEvent {

    private final Invitation invitation;
    private final AppUser user;
    private final Date timestamp;

    public InvitationCreatedEvent(Invitation invitation, AppUser user) {
        this.invitation = invitation;
        this.user = user;
        this.timestamp = new Date(System.currentTimeMillis());
    }
}