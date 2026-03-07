package hu.martinvass.dms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/**
 * Configuration class for creating an instance of Argon2PasswordEncoder as a Spring bean.
 */
@Configuration
public class PasswordEncoder {

    /**
     * Creates and configures an instance of Argon2PasswordEncoder as a Spring bean.
     *
     * @return An instance of Argon2PasswordEncoder.
     */
    @Bean
    public Argon2PasswordEncoder argon2PasswordEncoder() {
        return new Argon2PasswordEncoder(
                32,         // Hash length (in bytes)
                64,                   // Salt length (in bytes)
                1,                    // Number of iterations
                15 * 1024,            // Memory (in KB)
                10                    // Parallelism factor
        );
    }
}