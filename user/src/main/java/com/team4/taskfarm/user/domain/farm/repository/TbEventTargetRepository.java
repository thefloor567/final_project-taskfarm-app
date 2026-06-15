package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbEventTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TbEventTargetRepository extends JpaRepository<TbEventTarget, Long> {

    /** 이 이벤트가 노린 밭 목록 (이미 있으면 = 이미 처리됨 → 재타격 방지) */
    List<TbEventTarget> findByIdxFarmEvent(Long idxFarmEvent);

    /** 여러 밭의 타겟 (getFarm threat 표시 N+1 방지) */
    List<TbEventTarget> findByIdxPlotIn(List<Long> idxPlots);

    /** 업적 crow_defended / drought_survived — 방어 성공 횟수 (이벤트 종류별).*/
    @Query("select count(t) from TbEventTarget t, TbFarmEvent e " +
           "where t.idxFarmEvent = e.idxFarmEvent " +
           "  and e.idxUser = :idxUser " +
           "  and e.eventKey = :eventKey " +
           "  and t.isDefended = true")
    long countDefended(@Param("idxUser") Long idxUser, @Param("eventKey") String eventKey);
}