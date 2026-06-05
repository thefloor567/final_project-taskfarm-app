package com.team4.taskfarm.user.domain.stats.repository;

import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsExpLedgerRepository extends JpaRepository<TbExpLedger, Long> {

    @Query("""
            select e
            from TbExpLedger e
            where e.idxUser = :idxUser
              and e.type = :type
              and e.createDate between :startDateTime and :endDateTime
            order by e.createDate asc
            """)
    List<TbExpLedger> findEarnedExpBetween(
            @Param("idxUser") Long idxUser,
            @Param("type") TbExpLedger.LedgerType type,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);
}
