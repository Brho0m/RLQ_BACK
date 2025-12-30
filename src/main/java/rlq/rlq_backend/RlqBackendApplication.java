package rlq.rlq_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RlqBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RlqBackendApplication.class, args);
    }

}
