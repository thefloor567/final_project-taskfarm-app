package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.*;
import com.team4.taskfarm.user.domain.achievement.service.AchievementService;
import com.team4.taskfarm.user.domain.farm.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ThreatHandler {

    private final TbEventTargetRepository eventTargetRepository;
    private final TbPlotEffectRepository plotEffectRepository;
    private final TbCropRepository cropRepository;
    private final TbEventConfigRepository eventConfigRepository;
    private final AchievementService achievementService;

    /** 위협별 정책 정의 */
    private enum Threat {
        crow(TargetState.READY_ONLY, TbPlotEffect.EffectType.scarecrow, Damage.REMOVE),
        pest(TargetState.GROWING_ONLY, TbPlotEffect.EffectType.scarecrow, Damage.WITHER),
        drought(TargetState.BOTH, TbPlotEffect.EffectType.greenhouse, Damage.WITHER),
        storm(TargetState.BOTH, null, Damage.REMOVE);   // 방어 수단 null = 못 막음

        final TargetState target;
        final TbPlotEffect.EffectType defense;   // null이면 방어 불가
        final Damage damage;

        Threat(TargetState t, TbPlotEffect.EffectType d, Damage dmg) {
            this.target = t; this.defense = d; this.damage = dmg;
        }

        static Threat of(String key) {
            try { return valueOf(key); } catch (Exception e) { return null; }
        }
    }

    private enum TargetState { READY_ONLY, GROWING_ONLY, BOTH }
    private enum Damage { REMOVE, WITHER }

    /**
     * 오늘 위협 처리. getFarm 에서 밭/작물 로드 후 호출.
     */
    public void handleTodayThreat(TbFarmEvent event, List<TbPlot> plots, Map<Long, TbCrop> cropByPlot) {
        if (event == null) return;
        Threat threat = Threat.of(event.getEventKey());
        if (threat == null) return;   // 위협 아님(평범/보너스 등)

        // 이미 처리됐으면 스킵 (재타격 방지)
        if (!eventTargetRepository.findByIdxFarmEvent(event.getIdxFarmEvent()).isEmpty()) {
            return;
        }

        // 위협별 대상 후보: 작물 상태 필터 적용
        List<TbPlot> candidates = plots.stream()
                .filter(p -> {
                    TbCrop c = cropByPlot.get(p.getIdxPlot());
                    return c != null && matchesTarget(c, threat.target);
                })
                .toList();
        if (candidates.isEmpty()) return;   // 노릴 게 없으면 무효

        // 대상 밭 선정 (scope + 시드 고정)
        List<TbPlot> targets = pickTargets(event, candidates);

        // 타격
        LocalDate today = LocalDate.now();
        for (TbPlot plot : targets) {
            TbEventTarget target = eventTargetRepository.save(
                    TbEventTarget.of(event.getIdxFarmEvent(), plot.getIdxPlot()));
            TbCrop crop = cropByPlot.get(plot.getIdxPlot());

            boolean defended = tryDefend(plot, today, threat.defense);
            if (defended) {
                target.markDefended();   // IsDefended=1

                // 업적 체크: 방어 성공 (이벤트 종류별)
                String condType = switch (event.getEventKey()) {
                    case "crow"    -> "crow_defended";
                    case "drought" -> "drought_survived";
                    default        -> null;
                };
                if (condType != null) {
                    try {
                        achievementService.checkAndGrant(event.getIdxUser(), condType);
                    } catch (Exception e) {
                        // ThreatHandler는 getFarm 흐름 중 호출 → 조용히 무시
                    }
                }
            } else {
                applyDamage(crop, threat.damage);
            }
        }
    }

    /** 작물 상태가 위협 대상에 해당하나 */
    private boolean matchesTarget(TbCrop crop, TargetState target) {
        TbCrop.State s = crop.getState();
        return switch (target) {
            case READY_ONLY   -> s == TbCrop.State.ready;
            case GROWING_ONLY -> s == TbCrop.State.growing;
            case BOTH         -> s == TbCrop.State.growing || s == TbCrop.State.ready;
        };
    }

    /**
     * 방어 시도. defenseType 이 null(폭풍)이면 무조건 실패.
     * @return true = 방어 성공
     */
    private boolean tryDefend(TbPlot plot, LocalDate today, TbPlotEffect.EffectType defenseType) {
        if (defenseType == null) return false;   // 폭풍 — 방어 불가

        Optional<TbPlotEffect> effectOpt = plotEffectRepository
                .findByIdxPlotAndEffectType(plot.getIdxPlot(), defenseType)
                .filter(e -> !e.isExpired(today));
        if (effectOpt.isEmpty()) return false;

        TbPlotEffect effect = effectOpt.get();
        // 허수아비(횟수제)는 소모, 온실(기간제)은 유지
        if (defenseType == TbPlotEffect.EffectType.scarecrow) {
            boolean depleted = effect.consumeOnce();
            if (depleted) plotEffectRepository.delete(effect);
        }
        return true;
    }

    /** 피해 적용 */
    private void applyDamage(TbCrop crop, Damage damage) {
        if (crop == null) return;
        switch (damage) {
            case REMOVE -> cropRepository.delete(crop);   // 까마귀/폭풍 — 제거
            case WITHER -> crop.wither();                  // 해충/가뭄 — 시듦
        }
    }

    /**
     * scope 에 따라 대상 밭 선정. 날짜+유저 시드 고정 → 같은 날 같은 결과.
     * scope: one=1밭, multi=scopeCount밭, all=전부.
     */
    private List<TbPlot> pickTargets(TbFarmEvent event, List<TbPlot> candidates) {
        List<TbPlot> sorted = new ArrayList<>(candidates);
        sorted.sort(Comparator.comparing(TbPlot::getIdxPlot));
        Collections.shuffle(sorted, new Random(event.getIdxFarmEvent()));

        TbEventConfig cfg = eventConfigRepository
                .findFirstByEventKeyAndIsActiveTrue(event.getEventKey())
                .orElse(null);

        int count;
        if (cfg == null) {
            count = 1;
        } else {
            count = switch (cfg.getScope()) {
                case one   -> 1;
                case multi -> Math.max(1, cfg.getScopeCount());
                case all   -> sorted.size();
                case none  -> 0;
            };
        }
        return sorted.subList(0, Math.min(count, sorted.size()));
    }
}