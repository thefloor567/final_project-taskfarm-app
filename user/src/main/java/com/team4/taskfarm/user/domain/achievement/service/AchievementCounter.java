package com.team4.taskfarm.user.domain.achievement.service;

import com.team4.taskfarm.common.entity.farm.TbCoinLedger;
import com.team4.taskfarm.common.entity.farm.TbOrder;
import com.team4.taskfarm.common.entity.social.TbFriend;
import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.user.domain.ai.repository.AiLogRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbCoinLedgerRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbEventTargetRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbOrderRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbPlotRepository;
import com.team4.taskfarm.user.domain.friend.repository.FriendRepository;
import com.team4.taskfarm.user.domain.ranking.repository.RankSnapshotRepository;
import com.team4.taskfarm.user.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementCounter {

    private final TodoRepository todoRepository;
    private final TbFarmRepository farmRepository;
    private final TbPlotRepository plotRepository;
    private final TbOrderRepository orderRepository;
    private final TbCoinLedgerRepository coinLedgerRepository;
    private final AiLogRepository aiLogRepository;
    private final FriendRepository friendRepository;
    private final RankSnapshotRepository rankSnapshotRepository;
    private final TbEventTargetRepository eventTargetRepository;

    // ── 할일 ──
    public int todoDoneCount(Long idxUser) {
        return (int) todoRepository.countByIdxUserAndIsDoneTrueAndDeleteDateIsNull(idxUser);
    }

    public int priorityADoneCount(Long idxUser) {
        return (int) todoRepository.countByIdxUserAndPriorityAndIsDoneTrueAndDeleteDateIsNull(
                idxUser, TbTodo.Priority.A);
    }

    // ── 농장 (idxFarm 거침) ──
    public int harvestCount(Long idxUser) {
        // 누적 수확량 (TbFarm.TotalHarvest — harvest_counter_patch.md로 추가)
        return farmRepository.findByIdxUser(idxUser)
                .map(f -> f.getTotalHarvest())
                .orElse(0);
    }

    public int plotCount(Long idxUser) {
        Long idxFarm = farmIdOf(idxUser);
        if (idxFarm == null) return 0;
        return (int) plotRepository.countByIdxFarm(idxFarm);
    }

    public int coinEarnedTotal(Long idxUser) {
        Long idxFarm = farmIdOf(idxUser);
        if (idxFarm == null) return 0;
        return coinLedgerRepository.sumAmountByIdxFarmAndType(idxFarm, TbCoinLedger.LedgerType.EARN);
    }

    public int orderFulfillCount(Long idxUser) {
        Long idxFarm = farmIdOf(idxUser);
        if (idxFarm == null) return 0;
        return (int) orderRepository.countByIdxFarmAndState(idxFarm, TbOrder.State.DONE);
    }

    // ── 이벤트 방어 (tbEventTarget → tbFarmEvent 조인) ──
    public int crowDefendedCount(Long idxUser) {
        return (int) eventTargetRepository.countDefended(idxUser, "crow");
    }

    public int droughtSurvivedCount(Long idxUser) {
        return (int) eventTargetRepository.countDefended(idxUser, "drought");
    }

    // ── AI ──
    public int aiRecommendCount(Long idxUser) {
        return (int) aiLogRepository.countByIdxUser(idxUser);
    }

    // ── 친구 (ACCEPTED, 양방향) ──
    public int friendCount(Long idxUser) {
        long c = friendRepository.countByIdxUserAAndStatusOrIdxUserBAndStatus(
                idxUser, TbFriend.Status.ACCEPTED, idxUser, TbFriend.Status.ACCEPTED);
        return (int) c;
    }

    // ── 랭킹 (최소 ranking, 없으면 9999) ──
    public int bestWeeklyRank(Long idxUser) {
        return rankSnapshotRepository.findBestRanking(idxUser);
    }

    private Long farmIdOf(Long idxUser) {
        return farmRepository.findByIdxUser(idxUser)
                .map(f -> f.getIdxFarm())
                .orElse(null);
    }
}