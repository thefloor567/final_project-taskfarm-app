package com.team4.taskfarm.admin.domain.gemini.service;

import com.team4.taskfarm.admin.domain.gemini.dto.GeminiUsageResponse;
import com.team4.taskfarm.admin.domain.gemini.repository.AdminAiLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiMonitorService {

    private final AdminAiLogRepository aiLogRepository;

    // Gemini 호출당 예상 단가($). 실제 단가 확정 시 조정.
    private static final double COST_PER_CALL = 0.0012;

    @Transactional(readOnly = true)
    public GeminiUsageResponse getUsage() {
        long totalRequests = aiLogRepository.countTotalRequests();
        long actualCalls = aiLogRepository.countActualCalls();
        long cacheHits = totalRequests - actualCalls;
        double cacheRate = totalRequests > 0
                ? Math.round((double) cacheHits / totalRequests * 1000.0) / 10.0
                : 0.0;
        double monthCost = Math.round(actualCalls * COST_PER_CALL * 100.0) / 100.0;
        double savedCost = Math.round(cacheHits * COST_PER_CALL * 100.0) / 100.0;

        // 최근 7일 일별 캐시/실호출 (빈 날짜 0)
        Map<String, long[]> byDate = new HashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("M/d");
        for (Object[] r : aiLogRepository.findDailyCallStats()) {
            String date = r[0].toString();  // yyyy-MM-dd
            long cache = ((Number) r[1]).longValue();
            long actual = ((Number) r[2]).longValue();
            byDate.put(date, new long[]{cache, actual});
        }
        List<String> labels = new ArrayList<>();
        List<Long> dailyCache = new ArrayList<>();
        List<Long> dailyActual = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            String key = d.toString();  // yyyy-MM-dd
            labels.add(d.format(fmt));
            long[] v = byDate.getOrDefault(key, new long[]{0L, 0L});
            dailyCache.add(v[0]);
            dailyActual.add(v[1]);
        }

        return GeminiUsageResponse.builder()
                .totalRequests(totalRequests)
                .cacheHits(cacheHits)
                .actualCalls(actualCalls)
                .cacheRate(cacheRate)
                .monthCost(monthCost)
                .savedCost(savedCost)
                .dailyLabels(labels)
                .dailyCache(dailyCache)
                .dailyActual(dailyActual)
                .build();
    }
}
