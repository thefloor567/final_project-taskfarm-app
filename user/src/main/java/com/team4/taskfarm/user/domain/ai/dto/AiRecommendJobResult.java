package com.team4.taskfarm.user.domain.ai.dto;

// AI 추천 작업 상태 조회 결과.
public record AiRecommendJobResult(
        String jobId,
        AiRecommendJobStatus status,
        Integer rewardExp,
        Boolean isCache,
        String message
) {
    public static AiRecommendJobResult pending(String jobId) {
        return new AiRecommendJobResult(
                jobId,
                AiRecommendJobStatus.PENDING,
                null,
                null,
                "처리 중입니다."
        );
    }

    public static AiRecommendJobResult done(String jobId, int rewardExp, boolean isCache) {
        return new AiRecommendJobResult(
                jobId,
                AiRecommendJobStatus.DONE,
                rewardExp,
                isCache,
                "완료되었습니다."
        );
    }

    public static AiRecommendJobResult failed(String jobId, String message) {
        return new AiRecommendJobResult(
                jobId,
                AiRecommendJobStatus.FAILED,
                null,
                null,
                message
        );
    }
}