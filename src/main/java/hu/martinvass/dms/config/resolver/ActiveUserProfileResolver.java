package hu.martinvass.dms.config.resolver;

import hu.martinvass.dms.profile.CorporationProfile;
import hu.martinvass.dms.profile.repository.CorporationProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@AllArgsConstructor
public class ActiveUserProfileResolver implements HandlerMethodArgumentResolver {

    private final CorporationProfileRepository repository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(ActiveUserProfile.class)
                && parameter.getParameterType().equals(CorporationProfile.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        var request = (HttpServletRequest) webRequest.getNativeRequest();
        var session = request.getSession(false);

        if (session == null) {
            throw new IllegalStateException("No HTTP session available");
        }

        var profileId = (Long) session.getAttribute("ACTIVE_USER_PROFILE_ID");

        if (profileId == null) {
            return null;
//            throw new IllegalStateException("No active profile set in session");
        }

        return repository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("Active profile not found"));
    }
}