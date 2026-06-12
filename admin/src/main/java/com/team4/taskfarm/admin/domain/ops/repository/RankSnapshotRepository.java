package com.team4.taskfarm.admin.domain.ops.repository;

import com.team4.taskfarm.admin.domain.ops.dto.RankSnapshotRow;
import com.team4.taskfarm.common.entity.social.TbRankSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RankSnapshotRepository extends JpaRepository<TbRankSnapshot, Long> {

    // 주차별 확정 순위 조회 (닉네임은 tbUser 조인). 순위 오름차순.
    @Query("SELECT s.ranking as ranking, s.idxUser as userId, u.nickname as nickname, s.weeklyExp as weeklyExp " +
           "FROM TbRankSnapshot s, TbUser u " +
           "WHERE u.idxUser = s.idxUser AND s.period = :period " +
           "ORDER BY s.ranking ASC")
    List<RankSnapshotRow> findSnapshot(@Param("period") String period);
}
