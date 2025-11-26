package com.kesmarki.inticky.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.cache.annotation.EnableCaching;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
// import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Tenant Service Application
 * 
 * Manages multi-tenancy, organization administration, and tenant-specific configurations.
 * Port: 8081
 */
@SpringBootApplication
// @EnableJpaRepositories
// @EnableTransactionManagement
// @EnableCaching
public class TenantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantServiceApplication.class, args);
    }
}
