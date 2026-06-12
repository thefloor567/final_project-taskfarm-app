package com.team4.taskfarm.user.domain.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import com.team4.taskfarm.common.entity.exp.TbExpLedger.LedgerType;

/**
 * 경험치 거래내역 저장/조회.
 */
public interface TbExpLedgerRepository extends JpaRepository<TbExpLedger, Long> {
	
	// 특정 유저가 특정 Todo로 이미 TODO_DONE 경험치를 받은 적 있는지 확인
	boolean existsByIdxUserAndTypeAndReasonAndRefIdx(Long idxUser, LedgerType type, String reason, Long refIdx);
}

