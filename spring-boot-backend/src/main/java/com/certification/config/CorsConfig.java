package com.certification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局CORS跨域配置
 * 
 * 解决前后端分离时的跨域问题
 * 注意：生产环境应该配置具体的允许域名，而不是使用通配符
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许凭证（cookies）
        config.setAllowCredentials(true);
        
        // 允许的源（使用originPatterns而不是origins）
        // 开发环境：允许所有源
        config.addAllowedOriginPattern("*");
        
        // 生产环境建议配置具体域名：
        // config.addAllowedOriginPattern("https://yourdomain.com");
        // config.addAllowedOriginPattern("http://localhost:3000");
        // config.addAllowedOriginPattern("http://localhost:8080");
        
        // 允许的请求头
        config.addAllowedHeader("*");
        
        // 允许的HTTP方法
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("PATCH");
        
        // 暴露的响应头
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Accept");
        
        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);
        
        // 应用配置到所有路径
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
