package hu.martinvass.dms.annotations.resolver;

import hu.martinvass.dms.annotations.interceptor.CorporationRoleInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@AllArgsConstructor
public class ResolverConfig implements WebMvcConfigurer {

    private final ActiveUserProfileResolver activeResolver;
    private final CorporationRoleInterceptor roleInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(activeResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor);
    }
}