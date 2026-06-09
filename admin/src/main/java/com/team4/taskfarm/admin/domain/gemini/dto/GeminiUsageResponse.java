package com.team4.taskfarm.admin.domain.gemini.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GeminiUsageResponse {
    private long totalRequests;    // 총 추천 요청 (캐시+실호출)
    private long cacheHits;        // 캐시 적중 수
    private long actualCalls;      // 실제 Gemini 호출 (과금 대상)
    private double cacheRate;      // 캐시 적중률 0.0~100.0
    private double monthCost;      // 예상 비용($)
    private double savedCost;      // 캐시로 절약한 비용($)

    private List<String> dailyLabels;
    private List<Long> dailyCache;
    private List<Long> dailyActual;
}
