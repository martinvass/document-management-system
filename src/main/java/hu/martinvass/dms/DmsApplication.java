package hu.martinvass.dms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The main class that serves as the entry point for the application.
 */
@SpringBootApplication
@EnableAsync
public class DmsApplication {

    /**
     * The main method of the application.
     * It invokes SpringApplication's run method with the provided command-line arguments.
     *
     * @param args The command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(DmsApplication.class, args);
    }

}
