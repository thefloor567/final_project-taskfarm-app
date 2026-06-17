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
import com.team4.taskfarm.user.domain.achievement.service.AchievementService;
import com.team4.taskfarm.user.domain.ai.policy.ExpClampPolicy;
import com.team4.taskfarm.user.domain.ai.repository.AiLogRepository;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;
import com.team4.taskfarm.user.domain.ranking.service.RankingService;
import com.team4.taskfarm.user.domain.todo.repository.TbExpLedgerRepository;
import com.team4.taskfarm.user.domain.todo.repository.TbExpPolicyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardService {

    private final AuthUserRepository userRepository;
    private final TbFarmRepository farmRepository;
    private final TbExpPolicyRepository expPolicyRepository;
    private final TbExpLedgerRepository expLedgerRepository;
    private final AiLogRepository aiLogRepository;
    private final RankingService rankingService;
    private final AchievementService achievementService;

    /** 할일 완료 보상 지급 */
    @Transactional
    public void grantTodoDone(Long idxUser, TbTodo.Priority priority, Long idxTodo) {
    	
    	// 이미 이 할일로 경험치를 지급받은 적이 있으면 다시 지급 X
    	if (expLedgerRepository.existsByIdxUserAndTypeAndReasonAndRefIdx(idxUser, TbExpLedger.LedgerType.EARN, "TODO_DONE", idxTodo)) {
    	    return;
    	}
    	
    	TbExpPolicy policy = policyOf(priority);
    	int aiExp = getLatestAiRewardExp(idxUser, idxTodo);
    	int raw = (aiExp > 0) ? aiExp : policy.getBaseExp();
    	int granted = ExpClampPolicy.clamp(raw, policy.getMinExp(), policy.getMaxExp());  // ← DB값

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
        
        checkTodoAchievements(idxUser, priority);
    }
    
    /** 할일 완료 관련 업적 체크 (본 기능 보호: 실패 무시). */
    private void checkTodoAchievements(Long idxUser, TbTodo.Priority priority) {
        safeGrant(idxUser, "todo_done_total");
        if (priority == TbTodo.Priority.A) {
            safeGrant(idxUser, "priorityA_done");
        }
        safeGrant(idxUser, "streak_days");   // tbUser.Streak 읽음 (이미 갱신된 값)
        safeGrant(idxUser, "level_reach");   // tbUser.Level 읽음
    }
    
    /** 업적 1건 안전 호출. REQUIRES_NEW(AchievementService) + 예외 무시. */
    private void safeGrant(Long idxUser, String condType) {
        try {
            achievementService.checkAndGrant(idxUser, condType);
        } catch (Exception e) {
            log.warn("업적 체크 실패(무시) - user={}, cond={}, reason={}",
                    idxUser, condType, e.getMessage());
        }
    }

    /** 이 todo의 최신 AI 추천값 (없으면 0) */
    private int getLatestAiRewardExp(Long idxUser, Long idxTodo) {
        return aiLogRepository
                .findTopByIdxUserAndIdxTodoOrderByCreateDateDesc(idxUser, idxTodo)
                .map(TbAiLog::getRewardExp)
                .orElse(0);
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