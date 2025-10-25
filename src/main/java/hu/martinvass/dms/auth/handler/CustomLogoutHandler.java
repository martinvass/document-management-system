package hu.martinvass.dms.auth.handler;

import hu.martinvass.dms.audit.AuditEventAction;
import hu.martinvass.dms.audit.service.AuditService;
import hu.martinvass.dms.auth.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class CustomLogoutHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuditService auditService;
    private final AuthService authService;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication == null)
            response.sendRedirect("/auth/login");

        var url = request.getHeader("Referer");
        var user = authService.findByUsername(authentication.getName());

        // User logout audit log
        user.ifPresent(appUser -> auditService.log(
                AuditEventAction.USER_LOGOUT,
                appUser,
                String.format("Logged out: %s | From: %s", appUser.getUsername(), url)
        ));

        response.sendRedirect("/auth/login?logout");
    }
}