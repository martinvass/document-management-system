package hu.martinvass.dms.profile;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class ProfileSessionService {

    private static final String SESSION_ATTR = "ACTIVE_USER_PROFILE_ID";

    public void setActiveProfile(HttpSession session, Long id) {
        session.setAttribute(SESSION_ATTR, id);
    }

    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_ATTR);
    }
}