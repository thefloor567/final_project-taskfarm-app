package com.team4.taskfarm.common.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;

/**
 * 읽기/쓰기 DataSource 분리 설정.
 *
 * 흐름:
 *   master/replica DataSource 2개 생성
 *     → RoutingDataSource 로 묶음 (readOnly 면 replica)
 *       → LazyConnectionDataSourceProxy 로 감쌈 (커넥션 획득을 실제 쿼리 시점까지 지연)
 *
 * ⚠️ LazyConnectionDataSourceProxy 가 없으면, 스프링이 트랜잭션 시작 시
 *    readOnly 플래그가 설정되기 '전'에 커넥션을 잡아버려 라우팅이 안 먹는다.
 *    "설정했는데 다 master 로 간다"의 99% 원인.
 *
 * 활성 조건:
 *    application.yml 에 datasource.routing.enabled: true 가 있을 때만 동작.
 *
 * 로컬/인프라 전:
 *    application.yml 에서 master 와 replica 를 '같은 DB' 로 두면 분리 없이 정상 동작.
 *    RDS 읽기 엔드포인트가 생기면 replica 쪽 url 만 그 엔드포인트로 바꾸면 됨(코드 변경 0).
 */
@Configuration
@ConditionalOnProperty(name = "datasource.routing.enabled", havingValue = "true")
public class DataSourceConfig {

    /** 쓰기 DB (application.yml: spring.datasource.master.*) */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /** 읽기 DB (application.yml: spring.datasource.replica.*) */
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    /** 라우팅 DataSource: readOnly 플래그로 master/replica 선택 */
    @Bean
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("replicaDataSource") DataSource replica) {

        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(Map.of(
                DataSourceType.MASTER, master,
                DataSourceType.REPLICA, replica
        ));
        routing.setDefaultTargetDataSource(master); // 키 못 정하면 안전하게 master
        return routing;
    }

    /**
     * 실제 스프링이 쓰는 DataSource.
     * 라우팅 DataSource 를 Lazy 프록시로 감싸 커넥션 획득을 쿼리 시점까지 미룬다.
     * @Primary 라 JPA/MyBatis 등이 이걸 기본 사용.
     */
    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routing) {
        return new LazyConnectionDataSourceProxy(routing);
    }
}