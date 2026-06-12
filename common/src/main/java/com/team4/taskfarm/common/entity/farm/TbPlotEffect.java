package com.team4.taskfarm.common.entity.farm;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 밭별 도구 효과 (tbPlotEffect).
 * - 허수아비/온실/비료를 "특정 밭(Idx_Plot)"에 설치 (농장확장: 도구 밭별 적용).
 * - 위협 이벤트도 밭별로 판정 → 그 밭의 effect로 방어.
 * - 기존 tbFarm.ScarecrowLeft(농장 전역)는 deprecated, 신규 로직은 이 테이블만 사용.
 *
 * 도구별 의미:
 *   scarecrow  : 까마귀(crow) 방어. RemainUses 회 비축.
 *   greenhouse : 가뭄/시듦(drought·wither) 방어. 당일~기간(ExpireDate).
 *   fertilizer : 성장 가속(즉시 소모). 보통 설치 즉시 effect 소비.
 */
@Entity
@Table(name = "tbPlotEffect")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbPlotEffect extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_PlotEffect")
    private Long idxPlotEffect;

    @Column(name = "Idx_Plot", nullable = false)
    private Long idxPlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "EffectType", nullable = false)
    private EffectType effectType;

    @Column(name = "RemainUses", nullable = false)
    private int remainUses = 1;

    @Column(name = "ExpireDate")
    private LocalDate expireDate;   // 온실=당일, 비료=null(즉시), 허수아비=null(횟수제)

    public enum EffectType { scarecrow, greenhouse, fertilizer }

    /** 허수아비 설치 (까마귀 방어 횟수 비축). */
    public static TbPlotEffect scarecrow(Long idxPlot, int uses) {
        TbPlotEffect e = new TbPlotEffect();
        e.idxPlot = idxPlot;
        e.effectType = EffectType.scarecrow;
        e.remainUses = uses;
        return e;
    }

    /** 온실 설치 (가뭄/시듦 방어, 만료일까지). */
    public static TbPlotEffect greenhouse(Long idxPlot, LocalDate expireDate) {
        TbPlotEffect e = new TbPlotEffect();
        e.idxPlot = idxPlot;
        e.effectType = EffectType.greenhouse;
        e.remainUses = 1;
        e.expireDate = expireDate;
        return e;
    }

    /** 비료 설치 (성장 가속, 즉시 소모 전제). */
    public static TbPlotEffect fertilizer(Long idxPlot) {
        TbPlotEffect e = new TbPlotEffect();
        e.idxPlot = idxPlot;
        e.effectType = EffectType.fertilizer;
        e.remainUses = 1;
        return e;
    }

    /**
     * 방어 1회 소모. 남은 횟수를 1 줄이고, 소진 여부 반환(true=소진).
     * 소진된 effect는 서비스에서 삭제.
     */
    public boolean consumeOnce() {
        if (this.remainUses > 0) this.remainUses--;
        return this.remainUses <= 0;
    }

    public boolean isExpired(LocalDate today) {
        return expireDate != null && expireDate.isBefore(today);
    }
}