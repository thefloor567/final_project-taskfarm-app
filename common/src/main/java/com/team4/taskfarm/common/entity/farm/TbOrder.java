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
    
    /** 새 주민 주문 생성 (OPEN 상태) */
    public static TbOrder create(Long idxFarm, String villager, int reward) {
        TbOrder o = new TbOrder();
        o.idxFarm = idxFarm;
        o.villager = villager;
        o.reward = reward;
        o.state = State.OPEN;
        o.createDate = java.time.LocalDateTime.now();
        return o;
    }

    /** 주문 이행 처리 (OPEN -> DONE). 이미 완료면 예외. */
    public void fulfill() {
        if (state != State.OPEN) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("이미 완료된 주문입니다.");
        }
        state = State.DONE;
        doneDate = java.time.LocalDateTime.now();
    }
}