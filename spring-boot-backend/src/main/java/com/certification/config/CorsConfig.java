package com.certification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * CORS配置类
 * 解决跨域资源共享问题
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3100,http://127.0.0.1:3000,http://127.0.0.1:3100}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * 配置CORS跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        
        registry.addMapping("/**")
                // 允许的源
                .allowedOriginPatterns(origins.toArray(new String[0]))
                // 允许的HTTP方法
                .allowedMethods(allowedMethods.split(","))
                // 允许的请求头
                .allowedHeaders(allowedHeaders.split(","))
                // 允许的响应头
                .exposedHeaders(
                    "Access-Control-Allow-Origin",
                    "Access-Control-Allow-Methods", 
                    "Access-Control-Allow-Headers",
                    "Access-Control-Max-Age",
                    "Access-Control-Request-Headers",
                    "Access-Control-Request-Method",
                    "Content-Type",
                    "Authorization",
                    "X-Requested-With",
                    "Accept",
                    "Origin",
                    "Cache-Control",
                    "Pragma",
                    "Expires"
                )
                // 是否允许发送Cookie
                .allowCredentials(true)
                // 预检请求的有效期，单位为秒
                .maxAge(maxAge);
    }

    /**
     * CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        
        // 允许的响应头
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Methods", 
            "Access-Control-Allow-Headers",
            "Access-Control-Max-Age",
            "Access-Control-Request-Headers",
            "Access-Control-Request-Method",
            "Content-Type",
            "Authorization",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Cache-Control",
            "Pragma",
            "Expires"
        ));
        
        // 是否允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 预检请求的有效期，单位为秒
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
