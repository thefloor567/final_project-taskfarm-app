package com.team4.taskfarm.user.domain.ranking.scheduler;

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

    // 매주 일요일 23:59에 주간 랭킹 스냅샷 저장 0 * * * * *  0 59 23 * * SUN
    @Scheduled(cron = "0 59 23 * * SUN", zone = "Asia/Seoul")
    public void saveWeeklyRankSnapshot() {
        log.info("주간 랭킹 스냅샷 저장 시작");
        rankingService.saveWeeklyRankSnapshot();
        log.info("주간 랭킹 스냅샷 저장 종료");
    }
}