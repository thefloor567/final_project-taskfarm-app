package com.team4.taskfarm.user.domain.ai.service;

import org.springframework.stereotype.Service;

import com.team4.taskfarm.common.entity.todo.TbTodo.Priority;
import com.team4.taskfarm.user.domain.ai.client.GeminiClient;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendResult;
import com.team4.taskfarm.user.domain.ai.policy.ExpClampPolicy;

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
                    int finalExp = ExpClampPolicy.clamp(priority, cachedExp);
                    log.info("Redis 캐시 HIT - priority={}, title={}, exp={}",
                            priority, title, finalExp);
                    return new AiRecommendResult(finalExp, true, 0);
                }).orElseGet(() -> callGeminiAndCache(categoryName, priority, title));
    }
    
    // Redis 캐시가 없을 때 Gemini를 실제 호출하는 메서드
    private AiRecommendResult callGeminiAndCache(String categoryName, Priority priority, String title) {
        int rawExp;

        try {
            // Gemini 실제 호출
        	log.info("Redis 캐시 MISS - Gemini 호출 시작, priority={}, title={}",
                    priority, title);
            rawExp = geminiClient.recommendExp(
                    categoryName,
                    priority.name(),
                    title
            );
            
            log.info("Gemini 원본 응답 exp={}", rawExp);
            
        } catch (Exception e) {
            // Gemini 호출 실패 시 기본값 사용
        	log.warn("Gemini 호출 실패 - fallback 기본값 사용, priority={}, title={}",
                    priority, title, e);
            rawExp = ExpClampPolicy.baseExp(priority);
        }

        // Gemini or fallback 값을 우선순위 범위로 제한
        int finalExp = ExpClampPolicy.clamp(priority, rawExp);

        // 최종값을 Redis에 저장
        cacheService.put(categoryName, priority, title, finalExp);
        log.info("최종 추천 exp={}", finalExp);
        
        return new AiRecommendResult(finalExp, false, 0);
    }
}