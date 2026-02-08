package hu.martinvass.dms.events;

import hu.martinvass.dms.corporation.domain.Corporation;
import hu.martinvass.dms.user.AppUser;
import lombok.Getter;

import java.util.Date;

@Getter
public class CorporationCreatedEvent {

    private final AppUser user;
    private final Corporation corporation;
    private final Date timestamp;

    public CorporationCreatedEvent(AppUser user, Corporation corporation) {
        this.user = user;
        this.corporation = corporation;
        this.timestamp = new Date(System.currentTimeMillis());
    }
}