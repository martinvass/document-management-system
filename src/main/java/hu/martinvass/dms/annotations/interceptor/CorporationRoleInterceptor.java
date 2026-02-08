package hu.martinvass.dms.annotations.interceptor;

import hu.martinvass.dms.annotations.RequireCorpAdmin;
import hu.martinvass.dms.corporation.domain.CorporationRole;
import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class CorporationRoleInterceptor implements HandlerInterceptor {

    private final CorporationProfileRepository repository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod method) {
            RequireCorpAdmin annotation = method.getMethodAnnotation(RequireCorpAdmin.class);

            if (annotation != null) {
                HttpSession session = request.getSession(false);
                Long profileId = (Long) session.getAttribute("ACTIVE_USER_PROFILE_ID");

                CorporationProfile profile = repository.findById(profileId).orElse(null);

                if (profile == null)
                    return false;

                if (profile.getRole() != CorporationRole.ADMIN) {
                    response.sendRedirect("/access-denied");
                    return false;
                }
            }
        }

        return true;
    }
}