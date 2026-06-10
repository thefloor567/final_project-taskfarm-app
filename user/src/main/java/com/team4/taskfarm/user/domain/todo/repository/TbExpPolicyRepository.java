package com.team4.taskfarm.user.domain.todo.repository;

import com.team4.taskfarm.common.entity.exp.TbExpPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 경험치 정책(우선순위별 지급량) 조회.
 * A/B/C 3행 고정이라 전체 조회 후 Map 캐싱해서 쓴다 (조회 1회).
 * ⚠️ TbExpPolicy 의 실제 패키지 경로는 가정(common.entity.todo). 다르면 import 만 수정.
 */
public interface TbExpPolicyRepository extends JpaRepository<TbExpPolicy, Long> {
    // findAll() 사용 (3행뿐이라 전체 조회가 가장 단순)
    // 우선순위 매칭은 서비스에서 policy.getPriority().name() 으로 처리
}