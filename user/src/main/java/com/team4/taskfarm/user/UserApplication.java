package com.team4.taskfarm.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 유저앱 진입점.
 *
 * scanBasePackages: common의 Bean(JwtService 등) 스캔
 * @EntityScan: 엔티티가 common.entity 에 있으므로 스캔 범위에 포함
 *   - user 자기 도메인 + common 엔티티 둘 다 잡으려고 상위 패키지로
 * @EnableJpaRepositories: Repository도 마찬가지로 범위 지정
 */
@SpringBootApplication(scanBasePackages = "com.team4.taskfarm")
@EntityScan(basePackages = "com.team4.taskfarm.common.entity")
@EnableJpaRepositories(basePackages = "com.team4.taskfarm")
@EnableJpaAuditing
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}