package com.team4.taskfarm.user.domain.ranking.scheduler;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.team4.taskfarm.user.domain.ranking.service.RankingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 59 23 * * SUN", zone = "Asia/Seoul")
    public void saveWeeklyRankSnapshot() {
        // 분산 락: 여러 Pod 중 하나만 실행. SETNX(키 없을 때만 set)로 선점.
        String lockKey = "lock:weekly-snapshot:" + rankingService.currentPeriod();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", Duration.ofMinutes(10));

        if (!Boolean.TRUE.equals(locked)) {
            log.info("주간 랭킹 스냅샷 - 다른 Pod가 이미 실행 중, skip");
            return;
        }

        try {
            log.info("주간 랭킹 스냅샷 저장 시작 (락 획득)");
            rankingService.saveWeeklyRankSnapshot();
            log.info("주간 랭킹 스냅샷 저장 종료");
        } finally {
            // 작업 끝나면 락 해제 (안 풀어도 10분 TTL로 자동 만료)
            redisTemplate.delete(lockKey);
        }
    }
}