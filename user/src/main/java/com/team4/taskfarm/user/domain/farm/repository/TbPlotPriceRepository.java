package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbPlotPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbPlotPriceRepository extends JpaRepository<TbPlotPrice, Integer> {

    /** 밭 상점 진열용 — 슬롯 순 전체 */
    List<TbPlotPrice> findAllByOrderBySlotAsc();

    /** 특정 슬롯 가격 (구매 시) */
    Optional<TbPlotPrice> findBySlot(Integer slot);
}