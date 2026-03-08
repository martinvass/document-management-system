package hu.martinvass.dms.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
public class AppConfig {

    private String baseUrl;

    public String getVerificationUrl(String token) {
        return baseUrl + "/auth/verify?token=" + token;
    }

    public String getInvitationAcceptUrl(String token) {
        return baseUrl + "/invite/" + token;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl != null && baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}