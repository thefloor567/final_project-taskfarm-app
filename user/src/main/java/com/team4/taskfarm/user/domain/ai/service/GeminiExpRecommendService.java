package com.team4.taskfarm.user.domain.ai.service;

import org.springframework.stereotype.Service;

import com.team4.taskfarm.common.entity.todo.TbTodo.Priority;
import com.team4.taskfarm.user.domain.ai.client.GeminiClient;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiExpRecommendService {
	
	// 실제 Gemini API를 호출하는 클라이언트
    private final GeminiClient geminiClient;

    // Redis 캐시 조회/저장을 담당
    private final AiRecommendCacheService cacheService;
    
    // 경험치 추천 메서드
    // Redis 캐시 확인 -> 캐시 있으면 Gemini 호출 생략, 캐시 없으면 Gemini 호출 -> 최종 경험치를 clamp
    public AiRecommendResult recommendExp(String categoryName, Priority priority, String title) {
        return cacheService.get(categoryName, priority, title)
                .map(cachedExp -> {
                    // clamp 안 함 — 캐시된 raw 값 그대로 반환 (제한은 RewardService가 DB정책으로)
                    log.info("Redis 캐시 HIT - priority={}, title={}, exp={}", priority, title, cachedExp);
                    return new AiRecommendResult(cachedExp, true, 0);
                }).orElseGet(() -> callGeminiAndCache(categoryName, priority, title));
    }
    
    // Redis 캐시가 없을 때 Gemini를 실제 호출하는 메서드
    private AiRecommendResult callGeminiAndCache(String categoryName, Priority priority, String title) {
        int rawExp;
        try {
            log.info("Redis 캐시 MISS - Gemini 호출 시작, priority={}, title={}", priority, title);
            rawExp = geminiClient.recommendExp(categoryName, priority.name(), title);
            log.info("Gemini 원본 응답 exp={}", rawExp);
        } catch (Exception e) {
            // Gemini 호출 실패 시 0 반환 → RewardService가 DB의 baseExp를 fallback으로 씀
            log.warn("Gemini 호출 실패 - 0 반환(RewardService가 정책 baseExp 사용), priority={}, title={}",
                    priority, title, e);
            rawExp = 0;
        }
        // clamp 안 함 — raw 그대로 캐시 & 반환
        cacheService.put(categoryName, priority, title, rawExp);
        log.info("추천 exp(raw)={}", rawExp);

        return new AiRecommendResult(rawExp, false, 0);
    }
}