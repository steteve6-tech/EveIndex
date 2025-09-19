package com.certification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Knife4j配置类
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("认证监控系统API文档")
                        .description("认证监控系统后端API接口文档，包含爬虫数据管理、标准管理等功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("认证监控系统")
                                .email("admin@certification.com")
                                .url("https://github.com/certification-monitor"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发环境"),
                        new Server()
                                .url("https://api.certification.com")
                                .description("生产环境")
                ));
    }
}
