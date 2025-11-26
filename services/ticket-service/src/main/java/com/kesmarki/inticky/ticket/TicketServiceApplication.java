package com.kesmarki.inticky.ticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Ticket Service
 */
@SpringBootApplication(scanBasePackages = {
        "com.kesmarki.inticky.ticket",
        "com.kesmarki.inticky.common",
        "com.kesmarki.inticky.tenant",
        "com.kesmarki.inticky.security"
})
@EnableJpaAuditing
@EnableCaching
@EnableTransactionManagement
public class TicketServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketServiceApplication.class, args);
    }
}
