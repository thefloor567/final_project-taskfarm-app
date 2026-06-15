package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbOrderRepository extends JpaRepository<TbOrder, Long> {

    /** 농장의 진행중(OPEN) 주문 슬롯 */
    List<TbOrder> findByIdxFarmAndState(Long idxFarm, TbOrder.State state);

    /** 농장의 전체 주문 (상태 무관, 슬롯 표시용) */
    List<TbOrder> findByIdxFarmOrderByIdxOrderAsc(Long idxFarm);
    
    long countByIdxFarmAndState(Long idxFarm, TbOrder.State state);
}