package com.team4.taskfarm.user.domain.todo.service;

import com.team4.taskfarm.common.entity.exp.TbExpLedger;
import com.team4.taskfarm.common.entity.exp.TbExpPolicy;
import com.team4.taskfarm.common.entity.farm.TbFarm;
import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.repository.UserRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;
import com.team4.taskfarm.user.domain.todo.repository.TbExpLedgerRepository;
import com.team4.taskfarm.user.domain.todo.repository.TbExpPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 할일 완료에 따른 경험치·물방울 보상 (서버 권위).
 *
 * 정책:
 *  - XP 는 누적(tbUser.earnExp). auth 설계상 "절대 안 깎임" -> 완료 해제 시 회수 없음.
 *  - 완료: XP 적립 + 레벨업 판정(TbUser.earnExp 가 함께 처리) + 물방울 + 레벨업 보너스 물방울 + 원장 EARN.
 *
 * 경험치 지급량은 우선순위(A/B/C)별 tbExpPolicy 로 결정. (할일에 박지 않음)
 * 레벨 계산/레벨업 판정은 TbUser.earnExp() 가 단독 책임 - 여기서 레벨을 따로 계산하지 않는다.
 * 호출: TodoService.completeTodo 에서.
 */
@Service
@RequiredArgsConstructor
public class RewardService {

    private final UserRepository userRepository;
    private final TbFarmRepository farmRepository;
    private final TbExpPolicyRepository expPolicyRepository;
    private final TbExpLedgerRepository expLedgerRepository;

    /** 할일 완료 보상 지급 */
    @Transactional
    public void grantTodoDone(Long idxUser, TbTodo.Priority priority, Long idxTodo) {
        TbExpPolicy policy = policyOf(priority);

        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        // 1) XP 적립 + 레벨업 판정 (TbUser.earnExp 가 누적·레벨갱신까지 처리, 오른 레벨 수 반환)
        int gainedExp = policy.getBaseExp();
        int levelsGained = user.earnExp(gainedExp);

        // 2) 원장 EARN 기록 (잔고는 tbUser.Exp, 이력은 여기)
        expLedgerRepository.save(
                TbExpLedger.earn(idxUser, gainedExp, "TODO_DONE", idxTodo));

        // 3) 물방울 지급 (농장 자원이라 tbFarm)
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

    // 우선순위 -> 정책 (A/B/C 3행이라 findAll 후 매칭)
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