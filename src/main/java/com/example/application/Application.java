package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
        "com.example",
        "com.pragma.payment.application",
        "com.pragma.payment.infrastructure.controller"
})
@EnableJpaRepositories(basePackages = "com.example.infrastructure.persistence")
@EntityScan(basePackages = "com.example.infrastructure.persistence")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
