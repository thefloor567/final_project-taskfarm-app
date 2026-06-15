package com.team4.taskfarm.user.domain.ranking.repository;

import com.team4.taskfarm.common.entity.social.TbRankSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankSnapshotRepository extends JpaRepository<TbRankSnapshot, Long> {

    // 같은 주차 스냅샷이 이미 저장되어 있는지 확인 => 중복 방지
    boolean existsByPeriod(String period);

    @Query("select coalesce(min(r.ranking), 9999) from TbRankSnapshot r " +
           "where r.idxUser = :idxUser")
    int findBestRanking(@Param("idxUser") Long idxUser);
}