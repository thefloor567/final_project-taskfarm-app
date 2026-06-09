package com.team4.taskfarm.common.config.datasource;

/**
 * 읽기/쓰기 DataSource 라우팅 키.
 * @Transactional(readOnly=true) → REPLICA, 그 외 → MASTER.
 */
public enum DataSourceType {
    MASTER,   // 쓰기 (INSERT/UPDATE/DELETE, 또는 readOnly 아닌 트랜잭션)
    REPLICA   // 읽기 전용 (readOnly=true)
}