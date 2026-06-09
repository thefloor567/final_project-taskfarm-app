package com.team4.taskfarm.admin.domain.gemini.repository;

import com.team4.taskfarm.common.entity.ai.TbAiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdminAiLogRepository extends JpaRepository<TbAiLog, Long> {

    // 총 호출 요청 수 (캐시 + 실호출 모두)
    @Query("SELECT COUNT(a) FROM TbAiLog a")
    long countTotalRequests();

    // 실제 Gemini 호출 수 (캐시 미스 = 과금 대상)
    @Query("SELECT COUNT(a) FROM TbAiLog a WHERE a.isCache = false")
    long countActualCalls();

    // 최근 7일 일별 캐시/실호출 수
    @Query(value = "SELECT DATE(CreateDate) as dt, " +
                   "SUM(CASE WHEN IsCache = 1 THEN 1 ELSE 0 END) as cacheCnt, " +
                   "SUM(CASE WHEN IsCache = 0 THEN 1 ELSE 0 END) as actualCnt " +
                   "FROM tbAiLog " +
                   "WHERE CreateDate >= CURDATE() - INTERVAL 6 DAY " +
                   "GROUP BY DATE(CreateDate) ORDER BY dt ASC", nativeQuery = true)
    List<Object[]> findDailyCallStats();
}
