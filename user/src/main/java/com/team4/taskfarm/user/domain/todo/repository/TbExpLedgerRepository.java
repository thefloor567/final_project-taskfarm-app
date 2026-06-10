package com.team4.taskfarm.user.domain.todo.repository;

import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 경험치 거래내역 저장/조회.
 */
public interface TbExpLedgerRepository extends JpaRepository<TbExpLedger, Long> {
}