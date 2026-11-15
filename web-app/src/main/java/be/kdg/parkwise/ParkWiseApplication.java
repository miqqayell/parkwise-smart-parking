package be.kdg.parkwise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ParkWise Spring Boot application entry point.
 *
 * Bootstraps the application context and enables asynchronous method execution
 * for background tasks using {@link EnableAsync}.
 * Enables scheduled tasks for sensor monitoring using {@link EnableScheduling}.
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class ParkWiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkWiseApplication.class, args);
    }

}