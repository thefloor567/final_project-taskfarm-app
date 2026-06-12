package com.team4.taskfarm.user.domain.ranking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.farm.TbCoinLedger;
import com.team4.taskfarm.common.entity.farm.TbFarm;
import com.team4.taskfarm.user.domain.farm.repository.TbCoinLedgerRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyRankRewardService {

    private final TbFarmRepository farmRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;

    
    // 코인 원장에 남길 보상 사유.
    private static final String REASON_WEEKLY_RANK_REWARD = "WEEKLY_RANK_REWARD";

    // 주간 전체 랭킹 보상 지급.
    @Transactional
    public void grantWeeklyRankReward(Long idxUser, int rewardCoin, String period, int ranking, Long rankSnapshotId) {
        // 잘못된 값이면 지급하지 않는다.
        if (idxUser == null || rewardCoin <= 0 || rankSnapshotId == null) {
            return;
        }

        // 코인은 tbUser가 아니라 tbFarm에 저장
        TbFarm farm = farmRepository.findByIdxUser(idxUser).orElse(null);

        if (farm == null) {
            log.warn("주간 랭킹 보상 지급 실패 - 농장 없음. idxUser={}, period={}, ranking={}",
                    idxUser, period, ranking);
            return;
        }

        // 중복 지급 방지 => 같은 농장 + 같은 사유 + 같은 스냅샷 ID로 이미 지급된 기록이 있으면 다시 코인을 지급하지 않는다.
        boolean alreadyRewarded = coinLedgerRepository.existsByIdxFarmAndTypeAndReasonAndRefIdx(
                        farm.getIdxFarm(),
                        TbCoinLedger.LedgerType.EARN,
                        REASON_WEEKLY_RANK_REWARD,
                        rankSnapshotId
                );

        if (alreadyRewarded) {
            log.info("주간 랭킹 보상 이미 지급됨 - idxUser={}, period={}, ranking={}, snapshotId={}",
                    idxUser, period, ranking, rankSnapshotId);
            return;
        }

        // 실제 코인 증가.
        farm.earnCoin(rewardCoin);

        // 코인 지급 이력 저장.
        coinLedgerRepository.save(
                TbCoinLedger.earn(
                        farm.getIdxFarm(),
                        rewardCoin,
                        REASON_WEEKLY_RANK_REWARD,
                        rankSnapshotId
                )
        );

        log.info("주간 랭킹 보상 지급 완료 - period={}, ranking={}, idxUser={}, coin={}",
                period, ranking, idxUser, rewardCoin);
    }
}