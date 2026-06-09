package com.team4.taskfarm.user.domain.ai.dto;

public enum AiRecommendJobStatus {
    PENDING, // 접수됨, 아직 처리 전
    DONE,    // 처리 완료
    FAILED   // 처리 실패
}