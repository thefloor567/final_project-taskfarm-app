package com.team4.taskfarm.user.domain.ranking.service;

import com.team4.taskfarm.user.domain.mail.service.MailSendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyRankRewardService {

    private final MailSendService mailSendService;

    private static final String MAIL_SOURCE = "LEAGUE";

    /**
     * 주간 랭킹 보상 우편 발송.
     * refKey = "weekly_rank:{period}:{ranking}" 로 멱등(같은 주차·순위 중복 발송 차단).
     */
    public void grantWeeklyRankReward(Long idxUser, int rewardCoin, String period, int ranking) {
        if (idxUser == null || rewardCoin <= 0) {
            return;
        }

        String refKey = "weekly_rank:" + period + ":" + ranking;
        String title = "🏆 주간 랭킹 " + ranking + "위 보상";
        String body = period + " 주간 랭킹에서 " + ranking + "위를 달성했습니다! 코인 " + rewardCoin + "개를 받으세요.";

        boolean sent = mailSendService.sendCoinReward(
                idxUser, rewardCoin, title, body, MAIL_SOURCE, refKey);

        if (sent) {
            log.info("주간 랭킹 보상 우편 발송 - period={}, ranking={}, idxUser={}, coin={}",
                    period, ranking, idxUser, rewardCoin);
        }
    }
}