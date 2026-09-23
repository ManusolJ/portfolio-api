package com.portfolio.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import com.portfolio.interceptor.RateLimitInterceptor;

/** CORS rules and the interceptors that guard the public endpoints. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final RateLimitInterceptor rateLimitInterceptor;

    public WebConfig(CorsProperties corsProperties, RateLimitInterceptor rateLimitInterceptor) {
        this.corsProperties = corsProperties;
        this.rateLimitInterceptor = rateLimitInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")
            .allowedMethods("GET", "POST")
            .allowedOrigins(corsProperties.allowedOrigin());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
            .addInterceptor(rateLimitInterceptor)
            .addPathPatterns("/api/v1/contact");
    }
}
