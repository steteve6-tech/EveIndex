package com.certification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
// @EnableCaching  // 暂时禁用，因为 Redis 被禁用
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class CertificationMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificationMonitorApplication.class, args);
    }
}
