package hu.martinvass.dms.auth.handler;

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

    private final AuthService authService;

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (authentication == null)
            response.sendRedirect("/auth/login");

        response.sendRedirect("/auth/login?logout");
    }
}