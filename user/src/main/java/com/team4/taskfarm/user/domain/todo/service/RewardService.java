package com.team4.taskfarm.user.domain.todo.service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.ai.TbAiLog;
import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import com.team4.taskfarm.common.entity.exp.TbExpPolicy;
import com.team4.taskfarm.common.entity.farm.TbFarm;
import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.ai.repository.AiLogRepository;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;
import com.team4.taskfarm.user.domain.ranking.service.RankingService;
import com.team4.taskfarm.user.domain.todo.repository.TbExpLedgerRepository;
import com.team4.taskfarm.user.domain.todo.repository.TbExpPolicyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final AuthUserRepository userRepository;
    private final TbFarmRepository farmRepository;
    private final TbExpPolicyRepository expPolicyRepository;
    private final TbExpLedgerRepository expLedgerRepository;
    private final AiLogRepository aiLogRepository;          // ← 추가 (AI값 조회)
    private final RankingService rankingService;

    // 레벨디자인 ③ 클램프 범위 (TbExpPolicy에 없으므로 코드 상수)
    private static final Map<TbTodo.Priority, int[]> CLAMP = Map.of(
            TbTodo.Priority.A, new int[]{10, 60},
            TbTodo.Priority.B, new int[]{5, 30},
            TbTodo.Priority.C, new int[]{1, 15}
    );

    /** 할일 완료 보상 지급 */
    @Transactional
    public void grantTodoDone(Long idxUser, TbTodo.Priority priority, Long idxTodo) {
    	
    	// 이미 이 할일로 경험치를 지급받은 적이 있으면 다시 지급 X
    	if (expLedgerRepository.existsByIdxUserAndTypeAndReasonAndRefIdx(idxUser, TbExpLedger.LedgerType.EARN, "TODO_DONE", idxTodo)) {
    	    return;
    	}
    	
        TbExpPolicy policy = policyOf(priority);

        // AI 추천값 있으면 우선, 없으면 정책 BaseExp → clamp (레벨디자인 공식)
        int aiExp = getLatestAiRewardExp(idxUser, idxTodo);
        int raw = (aiExp > 0) ? aiExp : policy.getBaseExp();
        int granted = clamp(priority, raw);

        // 1) XP 적립 + 레벨업 판정
        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));
        int levelsGained = user.earnExp(granted);

        // 2) 원장 EARN 기록
        expLedgerRepository.save(
                TbExpLedger.earn(idxUser, granted, "TODO_DONE", idxTodo));
        
        // 3) 랭킹 점수 갱신 => 지급된 경험치를 기준으로 전체 누적 랭킹, 전체 주간 랭킹 Redis ZSET 점수를 증가
        rankingService.updateTotalScore(idxUser, user.getExp());
        rankingService.addWeeklyScore(idxUser, granted);

        // 4) 물방울 지급 (농장 자원이라 tbFarm)
        TbFarm farm = farmRepository.findByIdxUser(idxUser).orElse(null);
        if (farm != null) {
            if (policy.getDoneDrops() > 0) {
                farm.addDrops(policy.getDoneDrops());
            }
            if (levelsGained > 0 && policy.getLevelUpDrops() > 0) {
                farm.addDrops(policy.getLevelUpDrops() * levelsGained);
            }
        }
    }

    /** 이 todo의 최신 AI 추천값 (없으면 0) */
    private int getLatestAiRewardExp(Long idxUser, Long idxTodo) {
        return aiLogRepository
                .findTopByIdxUserAndIdxTodoOrderByCreateDateDesc(idxUser, idxTodo)
                .map(TbAiLog::getRewardExp)
                .orElse(0);
    }

    /** 우선순위별 MIN~MAX로 제한 */
    private int clamp(TbTodo.Priority priority, int raw) {
        int[] range = CLAMP.get(priority);
        return Math.max(range[0], Math.min(raw, range[1]));
    }

    // 우선순위 -> 정책
    private TbExpPolicy policyOf(TbTodo.Priority priority) {
        Map<String, TbExpPolicy> byPriority = expPolicyRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getPriority().name(), Function.identity(), (a, b) -> a));
        TbExpPolicy policy = byPriority.get(priority.name());
        if (policy == null) {
            throw CustomException.notFound("경험치 정책이 없습니다: " + priority);
        }
        return policy;
    }
}