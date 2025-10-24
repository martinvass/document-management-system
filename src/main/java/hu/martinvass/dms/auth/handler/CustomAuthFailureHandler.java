package hu.martinvass.dms.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    private final Map<Class<? extends AuthenticationException>, String> ERROR_MAP = Map.of(
            DisabledException.class, "not_verified",
            BadCredentialsException.class, "bad_credentials",
            LockedException.class, "locked"
    );

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        var param = ERROR_MAP.getOrDefault(exception.getClass(), "unknown");
        response.sendRedirect("/auth/login?error=" + param);
    }
}