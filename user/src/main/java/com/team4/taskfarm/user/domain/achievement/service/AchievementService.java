package com.team4.taskfarm.user.domain.achievement.service;

import com.team4.taskfarm.common.entity.farm.TbFarm;
import com.team4.taskfarm.common.entity.social.TbAchievement;
import com.team4.taskfarm.common.entity.social.TbUserAchievement;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.achievement.repository.TbAchievementRepository;
import com.team4.taskfarm.user.domain.achievement.repository.TbUserAchievementRepository;
import com.team4.taskfarm.user.domain.achievement.dto.AchievementItemResponse;
import com.team4.taskfarm.user.domain.achievement.dto.AchievementListResponse;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.farm.repository.TbFarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final TbAchievementRepository achievementRepository;
    private final TbUserAchievementRepository userAchievementRepository;
    private final AuthUserRepository userRepository;
    private final TbFarmRepository farmRepository;

    // 집계 출처 레포들 (각 도메인). 카운트 메서드는 currentValueOf 참고.
    private final AchievementCounter counter;   // 집계 전용 헬퍼(아래 별도 클래스)

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void checkAndGrant(Long idxUser, String condType) {
        List<TbAchievement> targets = achievementRepository.findByCondTypeAndIsActiveTrue(condType);
        if (targets.isEmpty()) return;

        int current = currentValueOf(idxUser, condType);

        for (TbAchievement ach : targets) {
            // 대부분 "많을수록 달성"(current >= condValue).
            // 단 weekly_rank(순위)는 "작을수록 달성" — 1위가 최고이므로 비교 방향 반대.
            boolean reached = "weekly_rank".equals(condType)
                    ? current <= ach.getCondValue()
                    : current >= ach.getCondValue();

            if (!reached) continue;
            if (userAchievementRepository.existsByIdxUserAndIdxAchievement(
                    idxUser, ach.getIdxAchievement())) continue;        // 이미 달성(멱등)
            grant(idxUser, ach);
        }
    }

    /** 실제 지급: 달성기록 + 코인. (칭호는 해금만, 장착은 별도 — equipTitle은 사용자가 선택) */
    private void grant(Long idxUser, TbAchievement ach) {
        try {
            // 1) 달성기록 (UNIQUE 위반 시 이미 달성 → 무시)
            userAchievementRepository.save(
                    TbUserAchievement.of(idxUser, ach.getIdxAchievement()));

            // 2) 코인 보상 (물방울 아님! 농장 코인)
            if (ach.getRewardCoin() > 0) {
                TbFarm farm = farmRepository.findByIdxUser(idxUser).orElse(null);
                if (farm != null) {
                    farm.earnCoin(ach.getRewardCoin());
                    // 코인 원장 기록은 farm 측 정책에 맞춰. 필요 시 CoinLedger.save 추가.
                }
            }
            log.info("업적 달성: user={}, code={}, coin={}", idxUser, ach.getCode(), ach.getRewardCoin());
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 UNIQUE 충돌 → 이미 누가 지급함. 멱등 보장됨.
            log.debug("업적 중복 달성 무시: user={}, code={}", idxUser, ach.getCode());
        }
    }

    /**
     * condType별 현재 집계값 (명세 3-5표 — 출처 고정).
     * 새 업적 종류 추가 시 여기에 case를 더한다.
     */
    private int currentValueOf(Long idxUser, String condType) {
        switch (condType) {
            case "todo_done_total":     return counter.todoDoneCount(idxUser);
            case "priorityA_done":      return counter.priorityADoneCount(idxUser);
            case "streak_days":         return userLevelOrStreak(idxUser, false);
            case "level_reach":         return userLevelOrStreak(idxUser, true);
            case "crop_harvest_total":  return counter.harvestCount(idxUser);
            case "plot_owned":          return counter.plotCount(idxUser);
            case "order_fulfill":       return counter.orderFulfillCount(idxUser);
            case "coin_earn_total":     return counter.coinEarnedTotal(idxUser);
            case "crow_defended":       return counter.crowDefendedCount(idxUser);
            case "drought_survived":    return counter.droughtSurvivedCount(idxUser);
            case "ai_recommend_use":    return counter.aiRecommendCount(idxUser);
            case "friend_count":        return counter.friendCount(idxUser);
            case "weekly_rank":         return counter.bestWeeklyRank(idxUser);  // 작을수록 좋음 주의
            default:
                log.warn("알 수 없는 업적 집계종류: {}", condType);
                return 0;
        }
    }

    /** 레벨/스트릭은 tbUser에서 바로. */
    private int userLevelOrStreak(Long idxUser, boolean wantLevel) {
        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));
        return wantLevel ? user.getLevel() : user.getStreak();
    }

    // =====================================================================
    // 조회 API
    // =====================================================================

    /** 업적 화면 데이터 (장착 칭호 + 카드 목록). */
    @Transactional(readOnly = true)
    public AchievementListResponse getAchievements(Long idxUser) {
        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        // 이 유저가 달성한 업적 ID 집합 (한 번에 조회)
        Set<Long> achievedIds = userAchievementRepository.findByIdxUser(idxUser).stream()
                .map(TbUserAchievement::getIdxAchievement)
                .collect(Collectors.toSet());

        List<AchievementItemResponse> items = achievementRepository.findByIsActiveTrue().stream()
                .map(ach -> {
                    boolean achieved = achievedIds.contains(ach.getIdxAchievement());
                    // 진행바: 달성이면 목표로 캡, 아니면 현재 집계값
                    int progress = achieved
                            ? ach.getCondValue()
                            : Math.min(currentValueOf(idxUser, ach.getCondType()), ach.getCondValue());
                    return AchievementItemResponse.builder()
                            .code(ach.getCode())
                            .name(ach.getName())
                            .title(ach.getTitle())
                            .category(ach.getCategory().name())
                            .condValue(ach.getCondValue())
                            .progress(progress)
                            .achieved(achieved)
                            .rewardCoin(ach.getRewardCoin())
                            .build();
                })
                .collect(Collectors.toList());

        return AchievementListResponse.builder()
                .equippedTitle(user.getEquippedTitle())
                .list(items)
                .build();
    }

    /**
     * 칭호 장착. 본인이 보유(달성)한 칭호만 장착 가능.
     * - title이 비면 장착 해제.
     */
    @Transactional
    public void equipTitle(Long idxUser, String title) {
        TbUser user = userRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        if (title == null || title.isBlank()) {
            user.equipTitle(null);   // 해제
            return;
        }

        // 보유 검증: 내가 달성한 업적들의 칭호 중에 title이 있는지
        Set<Long> achievedIds = userAchievementRepository.findByIdxUser(idxUser).stream()
                .map(TbUserAchievement::getIdxAchievement)
                .collect(Collectors.toSet());

        boolean owned = achievementRepository.findByIsActiveTrue().stream()
                .filter(a -> achievedIds.contains(a.getIdxAchievement()))
                .anyMatch(a -> a.getTitle().equals(title));

        if (!owned) {
            throw CustomException.badRequest("보유하지 않은 칭호는 장착할 수 없습니다.");
        }
        user.equipTitle(title);
    }
}