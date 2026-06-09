package com.team4.taskfarm.common.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 현재 트랜잭션이 readOnly 면 REPLICA, 아니면 MASTER 로 라우팅.
 *
 *
 * ⚠️ DataSourceConfig 에서
 *    LazyConnectionDataSourceProxy 로 감싸야 readOnly 플래그가 설정된 뒤
 *    커넥션을 획득해 라우팅이 올바르게 동작한다.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return readOnly ? DataSourceType.REPLICA : DataSourceType.MASTER;
    }
}