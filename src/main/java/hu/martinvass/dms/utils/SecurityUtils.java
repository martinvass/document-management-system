package hu.martinvass.dms.utils;

import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@UtilityClass
public class SecurityUtils {

    public boolean isAuthenticated() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && !AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass()) && authentication.isAuthenticated();
    }
}