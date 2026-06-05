package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbOrderItemRepository extends JpaRepository<TbOrderItem, Long> {

    /** 주문의 요구 작물 목록 */
    List<TbOrderItem> findByIdxOrder(Long idxOrder);

    /** 여러 주문의 요구 작물 한 번에 (N+1 방지) */
    List<TbOrderItem> findByIdxOrderIn(List<Long> idxOrders);
}