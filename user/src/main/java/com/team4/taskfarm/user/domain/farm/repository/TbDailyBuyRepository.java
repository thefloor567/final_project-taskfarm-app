package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbDailyBuy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TbDailyBuyRepository extends JpaRepository<TbDailyBuy, Long> {

    /** 오늘 (농장+타입+아이템) 구매 기록 — 있으면 cnt 증가, 없으면 신규 */
    Optional<TbDailyBuy> findByIdxFarmAndItemTypeAndItemIdxAndBuyDate(
            Long idxFarm, TbDailyBuy.ItemType itemType, Long itemIdx, LocalDate buyDate);
}