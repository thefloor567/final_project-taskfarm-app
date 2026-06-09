package com.team4.taskfarm.user.domain.ai.service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobRequest;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendQueueService {

    // Redis List 큐 이름
    private static final String QUEUE_KEY = "ai:recommend:queue";

    // job 상태/결과 저장 key prefix
    private static final String JOB_KEY_PREFIX = "ai:recommend:job:";

    // job 결과 보관 기간
    private static final Duration JOB_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 추천 작업 접수
    // jobId 생성 -> job 상태를 Pending 으로 저장 -> Redis Queue에 작업 적재 -> jobId 반환
    public AiRecommendJobResult enqueue(AiRecommendJobRequest requestWithoutJobId) {
        try {
            String jobId = UUID.randomUUID().toString();

            AiRecommendJobRequest request = new AiRecommendJobRequest(
                    jobId,
                    requestWithoutJobId.idxUser(),
                    requestWithoutJobId.idxTodo(),
                    requestWithoutJobId.categoryName(),
                    requestWithoutJobId.priority(),
                    requestWithoutJobId.title()
            );

            saveJobResult(AiRecommendJobResult.pending(jobId));

            // 작업을 JSON 문자열로 바꿔 Redis List에 넣음
            String json = objectMapper.writeValueAsString(request);

            // 오른쪽에 넣고, Worker가 왼쪽에서 꺼내는 구조로 사용할 예정
            redisTemplate.opsForList().rightPush(QUEUE_KEY, json);

            log.info("AI 추천 작업 접수 - jobId={}, idxTodo={}", jobId, request.idxTodo());

            return AiRecommendJobResult.pending(jobId);

        } catch (Exception e) {
            log.error("AI 추천 작업 접수 실패", e);
            throw new IllegalStateException("AI 추천 작업 접수에 실패했습니다.", e);
        }
    }

    // jobId로 현재 작업 상태/결과 조회
    public AiRecommendJobResult getJobResult(String jobId) {
        try {
            String key = jobKey(jobId);
            String json = redisTemplate.opsForValue().get(key);

            if (json == null) {
                return AiRecommendJobResult.failed(jobId, "작업 정보를 찾을 수 없습니다.");
            }

            return objectMapper.readValue(json, AiRecommendJobResult.class);

        } catch (Exception e) {
            log.error("AI 추천 작업 조회 실패 - jobId={}", jobId, e);
            return AiRecommendJobResult.failed(jobId, "작업 조회에 실패했습니다.");
        }
    }

    // Worker가 처리 완료/실패 시 결과를 저장할 때도 사용할 메서드
    public void saveJobResult(AiRecommendJobResult result) {
        try {
            String key = jobKey(result.jobId());
            String json = objectMapper.writeValueAsString(result);

            redisTemplate.opsForValue().set(key, json, JOB_TTL);

        } catch (Exception e) {
            log.error("AI 추천 작업 결과 저장 실패 - jobId={}", result.jobId(), e);
            throw new IllegalStateException("AI 추천 작업 결과 저장에 실패했습니다.", e);
        }
    }

    private String jobKey(String jobId) {
        return JOB_KEY_PREFIX + jobId;
    }
    
    
    //Redis Queue에서 AI 추천 작업 하나를 꺼냄 => 큐에 작업이 있으면 JSON 문자열 → AiRecommendJobRequest 객체로 변환해서 반환, 큐가 비어 있으면 Optional.empty() 반환
    public Optional<AiRecommendJobRequest> popJob() {
        try {
            // Redis List의 왼쪽에서 작업 하나 꺼내기
            String json = redisTemplate.opsForList().leftPop(QUEUE_KEY);

            if (json == null) {
                return Optional.empty();
            }

            // JSON -> Java 객체로 변환
            AiRecommendJobRequest request =
                    objectMapper.readValue(json, AiRecommendJobRequest.class);

            return Optional.of(request);

        } catch (Exception e) {
            // Redis 연결 문제나 JSON 변환 실패가 나도 서버 전체가 죽지 않게 empty 처리
            log.error("AI 추천 작업 큐 조회 실패", e);
            return Optional.empty();
        }
    }
}