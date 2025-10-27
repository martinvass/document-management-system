package hu.martinvass.dms.config;

import hu.martinvass.dms.auth.AuthService;
import hu.martinvass.dms.auth.handler.CustomAuthFailureHandler;
import hu.martinvass.dms.auth.handler.CustomLogoutHandler;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class for Spring Security.
 */
@EnableWebSecurity
@Configuration
@AllArgsConstructor
public class WebSecurityConfig {

    private SessionConfig sessionConfig;

    private final CustomLogoutHandler logoutHandler;
    private final CustomAuthFailureHandler authFailureHandler;

    private final AuthService authService;
    private final Argon2PasswordEncoder passwordEncoder;

    private final String[] ENDPOINTS_WHITELIST = {
            "/",
            "/css/**",
            "/js/**",
            "/auth/login",
            "/auth/sign-up",
            "/auth/verify",
            "/auth/verification-failed"
    };

    private final String LOGIN_URL = "/auth/login";
    private final String DEFAULT_SUCCESS_URL = "/home";

    /**
     * Configures Spring Security's filter chain.
     *
     * @param http HttpSecurity instance to configure.
     * @return Configured SecurityFilterChain instance.
     * @throws Exception If an error occurs while configuring.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ENDPOINTS_WHITELIST).permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage(LOGIN_URL)
                        .loginProcessingUrl(LOGIN_URL)
                        .defaultSuccessUrl(DEFAULT_SUCCESS_URL, true)
                        .failureHandler(authFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutHandler)
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(e -> e.accessDeniedPage("/access-denied"))
                .sessionManagement(s -> s.maximumSessions(1).sessionRegistry(sessionConfig.sessionRegistry()).maxSessionsPreventsLogin(true).expiredUrl("/auth/login?expired"))
                .httpBasic(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * Creates and configures an AuthenticationManager.
     *
     * @param http HttpSecurity instance used to access the AuthenticationManagerBuilder.
     * @return AuthenticationManager instance.
     * @throws Exception If an error occurs while configuring.
     */
    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(daoAuthenticationProvider());

        return authenticationManagerBuilder.build();
    }

    /**
     * Creates and configures a DaoAuthenticationProvider.
     *
     * @return Configured DaoAuthenticationProvider instance.
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(authService);

        return provider;
    }
}