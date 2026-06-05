package com.team4.taskfarm.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 어드민앱 진입점.
 * 엔티티는 common.entity 공유. user앱과 같은 스캔 설정.
 */
@SpringBootApplication(scanBasePackages = "com.team4.taskfarm")
@EntityScan(basePackages = "com.team4.taskfarm.common.entity")
@EnableJpaRepositories(basePackages = "com.team4.taskfarm")
@EnableJpaAuditing
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}