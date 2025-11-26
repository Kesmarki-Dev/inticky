package com.kesmarki.inticky.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * User Service Application
 * 
 * Manages users, roles, permissions, and authentication.
 * Port: 8082
 */
@SpringBootApplication(scanBasePackages = {
    "com.kesmarki.inticky.user",
    "com.kesmarki.inticky.shared",
    "com.kesmarki.inticky.security",
    "com.kesmarki.inticky.tenant"
})
@EnableJpaRepositories
@EnableTransactionManagement
@EnableCaching
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}