package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbPlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbPlotRepository extends JpaRepository<TbPlot, Long> {

    /** 농장의 밭 슬롯 전체 (슬롯 순) */
    List<TbPlot> findByIdxFarmOrderBySlotAsc(Long idxFarm);
}