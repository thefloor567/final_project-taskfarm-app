package com.team4.taskfarm.user.domain.ranking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.social.TbRankSnapshot;

public interface RankSnapshotRepository extends JpaRepository<TbRankSnapshot, Long> {

	// 같은 주차 스냅샷이 이미 지정되어 있는지 확인 => 중복 방지
	boolean existsByPeriod(String period);
}
