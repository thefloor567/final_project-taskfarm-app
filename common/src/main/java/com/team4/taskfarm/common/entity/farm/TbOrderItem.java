package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 요구작물 상세 (tbOrderItem). 주문 1:N 작물.
 */
@Entity
@Table(name = "tbOrderItem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_OrderItem")
    private Long idxOrderItem;

    @Column(name = "Idx_Order", nullable = false)
    private Long idxOrder;

    @Column(name = "Idx_Seed", nullable = false)
    private Long idxSeed;

    @Column(name = "Qty", nullable = false)
    private int qty;
}