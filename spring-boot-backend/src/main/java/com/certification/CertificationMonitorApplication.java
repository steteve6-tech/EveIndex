package com.certification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
@EnableTransactionManagement
public class CertificationMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertificationMonitorApplication.class, args);
    }
}
