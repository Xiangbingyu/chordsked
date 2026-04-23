package com.chordsked.backend.idempotent.config;

import com.chordsked.backend.idempotent.interceptor.IdempotentInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class IdempotentMvcConfig implements WebMvcConfigurer {
    @Resource(name = "idempotentInterceptor")
    private IdempotentInterceptor idempotentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotentInterceptor).addPathPatterns("/admin/api/v1/**");
    }
}
