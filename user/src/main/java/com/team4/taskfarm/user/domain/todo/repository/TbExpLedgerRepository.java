package com.team4.taskfarm.user.domain.todo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import com.team4.taskfarm.common.entity.exp.TbExpLedger.LedgerType;

/**
 * 경험치 거래내역 저장/조회.
 */
public interface TbExpLedgerRepository extends JpaRepository<TbExpLedger, Long> {

	// 특정 유저가 특정 Todo로 이미 TODO_DONE 경험치를 받은 적 있는지 확인
	boolean existsByIdxUserAndTypeAndReasonAndRefIdx(Long idxUser, LedgerType type, String reason, Long refIdx);

	// 특정 유저가 특정 Todo로 받은 TODO_DONE 원장 1건 조회.
	// "실제로 지급된 경험치 값"을 화면에 정직하게 표시하기 위해 사용. (완료는 1회만 지급 → 사실상 단건)
	Optional<TbExpLedger> findByIdxUserAndTypeAndReasonAndRefIdx(Long idxUser, LedgerType type, String reason, Long refIdx);

	// 목록 조회용: 유저의 TODO_DONE 지급 내역 전체 (refIdx=할일Idx 기준으로 매핑해 N+1 방지)
	List<TbExpLedger> findByIdxUserAndTypeAndReason(Long idxUser, LedgerType type, String reason);
}