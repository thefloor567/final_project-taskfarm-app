package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbPlotEffect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbPlotEffectRepository extends JpaRepository<TbPlotEffect, Long> {

    /** 특정 밭의 특정 타입 effect (시듦 방어용 greenhouse, 까마귀 방어용 scarecrow 조회) */
    Optional<TbPlotEffect> findByIdxPlotAndEffectType(Long idxPlot, TbPlotEffect.EffectType effectType);

    /** 여러 밭의 effect 한 번에 (getFarm 시듦 판정 N+1 방지) */
    List<TbPlotEffect> findByIdxPlotIn(List<Long> idxPlots);

    /** 특정 밭의 모든 effect */
    List<TbPlotEffect> findByIdxPlot(Long idxPlot);
}