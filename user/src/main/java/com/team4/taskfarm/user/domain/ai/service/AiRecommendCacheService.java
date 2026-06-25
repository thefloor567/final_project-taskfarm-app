package com.team4.taskfarm.user.domain.ai.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.team4.taskfarm.common.entity.todo.TbTodo.Priority;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiRecommendCacheService {

    // Redis에 저장될 key 앞부분
    private static final String CACHE_PREFIX = "ai:recommend:exp:";

    // 캐시 보관 기간
    private static final Duration TTL = Duration.ofDays(7);

    // Redis에 String 값을 저장/조회하기 위한 객체
    private final StringRedisTemplate redisTemplate;

    // Prometheus 메트릭 등록용 객체
    private final MeterRegistry meterRegistry;

    // Redis에서 캐시된 추천 경험치를 조회
    public Optional<Integer> get(String categoryName, Priority priority, String title) {
        try {
            String key = createKey(categoryName, priority, title);
            String value = redisTemplate.opsForValue().get(key);

            // 값이 없으면 캐시 미스
            if (value == null) {
                meterRegistry.counter("ai_cache", "result", "miss").increment();
                return Optional.empty();
            }

            // 값이 있으면 캐시 히트
            meterRegistry.counter("ai_cache", "result", "hit").increment();
            return Optional.of(Integer.parseInt(value));

        } catch (Exception e) {
            // Redis 조회 실패도 캐시 미스로 처리
            meterRegistry.counter("ai_cache", "result", "miss").increment();
            log.warn("Redis 캐시 조회 실패 - 캐시 미스로 처리", e);
            return Optional.empty();
        }
    }

    // 최종 추천 경험치를 Redis에 저장
    public void put(String categoryName, Priority priority, String title, int rewardExp) {
        try {
            String key = createKey(categoryName, priority, title);

            // redis에 저장
            redisTemplate.opsForValue().set(
                    key,
                    String.valueOf(rewardExp),
                    TTL);

        } catch (Exception e) {
            log.warn("Redis 캐시 저장 실패", e);
        }
    }

    // 캐시 key 생성 => 같은 카테고리 + 같은 우선순위 + 같은 제목이면 같은 key가 생성
    private String createKey(String categoryName, Priority priority, String title) {
        String raw = normalize(categoryName)
                + "|"
                + priority.name()
                + "|"
                + normalize(title);

        return CACHE_PREFIX + sha256(raw);
    }

    // 입력값 정규화 => 앞뒤 공백 제거 + 소문자 변환을 한다.
    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value.trim().toLowerCase();
    }

    // 문자열을 SHA-256 해시로 변경 => Redis key가 너무 길어지거나 한글/공백 등이 섞이는 것을 피하기 위해 사용
    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException("캐시 키 생성 실패", e);
        }
    }
}