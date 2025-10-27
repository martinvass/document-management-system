package hu.martinvass.dms.events;

import hu.martinvass.dms.user.AppUser;
import lombok.Getter;

import java.util.Date;

@Getter
public class UserRegisteredEvent {

    private final AppUser user;
    private final Date timestamp;

    public UserRegisteredEvent(AppUser user) {
        this.user = user;
        this.timestamp = new Date(System.currentTimeMillis());
    }
}