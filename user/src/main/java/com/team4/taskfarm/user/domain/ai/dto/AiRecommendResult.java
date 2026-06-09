package com.team4.taskfarm.user.domain.ai.dto;

// AI 경험치 추천 결과를 담는 DTO => Redis 캐싱을 적용하면서 캐시에서 나온 값인지도 알아야 함
public record AiRecommendResult(
        int rewardExp,
        boolean isCache,
        int token
) {
}