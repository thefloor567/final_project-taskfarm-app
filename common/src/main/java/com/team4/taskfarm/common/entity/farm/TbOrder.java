package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 주민 주문 슬롯 (tbOrder). 이행 시 작물 차감→코인 지급, 슬롯 새 주문 교체.
 */
@Entity
@Table(name = "tbOrder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Order")
    private Long idxOrder;

    @Column(name = "Idx_Farm", nullable = false)
    private Long idxFarm;

    @Column(name = "Villager", nullable = false, length = 30)
    private String villager;

    @Column(name = "Reward", nullable = false)
    private int reward;

    @Enumerated(EnumType.STRING)
    @Column(name = "State", nullable = false)
    private State state = State.OPEN;

    @Column(name = "CreateDate", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "DoneDate")
    private LocalDateTime doneDate;

    public enum State { OPEN, DONE }

    // ✏️ TODO: fulfill()
}