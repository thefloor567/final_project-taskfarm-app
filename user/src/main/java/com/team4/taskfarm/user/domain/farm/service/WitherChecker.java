package com.team4.taskfarm.user.domain.farm.service;

import com.team4.taskfarm.common.entity.farm.TbCrop;
import com.team4.taskfarm.common.entity.farm.TbPlotEffect;
import com.team4.taskfarm.user.domain.farm.repository.TbPlotEffectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 시듦(wither) 판정을 한 곳에 모은 컴포넌트 (lazy 판정).
 *
 * 작물을 "읽는" 모든 지점(getFarm 등)에서 이걸 거쳐야 화면마다 상태가 달라지지 않는다.
 *
 * 동작 (농장확장 반영):
 *  - growing 작물이 기준 시간(witherMinutes)을 넘겼으면 시들 차례
 *  - 그 작물의 밭에 '온실(greenhouse) effect'가 있으면 방어(시듦 막고 타이머 리셋)
 *  - 없으면 withered 전환
 *
 * ⚠️ 허수아비(scarecrow)는 더 이상 시듦을 막지 않는다 — 까마귀 방어 전용(CrowHandler).
 *    시듦 방어는 '온실'만. (설계서: 허수아비=crow, 온실=drought/wither)
 *
 * 기준 시간은 설정값(application.yml, 분 단위) → 테스트 2분 / 운영 1440분(24h).
 */
@Component
@RequiredArgsConstructor
public class WitherChecker {

    private final TbPlotEffectRepository plotEffectRepository;

    @Value("${farm.wither-minutes:1440}")
    private long witherMinutes;

    /**
     * 작물 목록에 대해 시듦 판정 적용. 상태가 바뀐 작물이 있으면 호출측에서 저장(@Transactional dirty checking).
     * 각 작물의 밭(idxPlot)에 온실 effect가 있으면 방어.
     *
     * @return 상태가 변경된 작물이 하나라도 있으면 true
     */
    public boolean applyWither(List<TbCrop> crops) {
        if (crops == null || crops.isEmpty()) return false;

        // 시들 차례인 작물만 추림
        List<TbCrop> due = crops.stream()
                .filter(c -> c.isWitherDue(witherMinutes))
                .toList();
        if (due.isEmpty()) return false;

        // 해당 밭들의 온실 effect 한 번에 조회 (N+1 방지)
        List<Long> plotIds = due.stream().map(TbCrop::getIdxPlot).distinct().toList();
        LocalDate today = LocalDate.now();
        Map<Long, TbPlotEffect> greenhouseByPlot = plotEffectRepository.findByIdxPlotIn(plotIds).stream()
                .filter(e -> e.getEffectType() == TbPlotEffect.EffectType.greenhouse)
                .filter(e -> !e.isExpired(today))   // 만료된 온실은 방어 못 함
                .collect(Collectors.toMap(TbPlotEffect::getIdxPlot, Function.identity(), (a, b) -> a));

        boolean changed = false;
        for (TbCrop crop : due) {
            TbPlotEffect greenhouse = greenhouseByPlot.get(crop.getIdxPlot());
            if (greenhouse != null) {
                crop.protectFromWither();   // 온실 방어 — 타이머 리셋, 시들지 않음
                // 온실은 기간제(당일)라 1회 방어로 소모하지 않음. 만료일까지 유효.
            } else {
                crop.wither();
            }
            changed = true;
        }
        return changed;
    }

    /** 단건 판정 (필요 시) */
    public boolean applyWither(TbCrop crop) {
        return applyWither(List.of(crop));
    }

    public long getWitherMinutes() {
        return witherMinutes;
    }
}