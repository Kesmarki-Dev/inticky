package com.kesmarki.inticky.tenant.config;

import com.kesmarki.inticky.tenant.interceptor.TenantInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for tenant context management
 */
@Configuration
@RequiredArgsConstructor
public class TenantContextConfiguration implements WebMvcConfigurer {
    
    private final TenantInterceptor tenantInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/actuator/**",
                    "/health/**",
                    "/error",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/auth/**",
                    "/api/tickets/**",
                    "/api/users/**"
                );
    }
}
