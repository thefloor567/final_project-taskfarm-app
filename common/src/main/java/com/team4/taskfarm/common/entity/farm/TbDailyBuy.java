package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 하루 구매 기록 (tbDailyBuy). 서버 권위로 하루한도 검증(무한구매 차단).
 * (농장,상품종류,상품인덱스,날짜) 조합 유일(UK).
 */
@Entity
@Table(name = "tbDailyBuy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbDailyBuy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_DailyBuy")
    private Long idxDailyBuy;

    @Column(name = "Idx_Farm", nullable = false)
    private Long idxFarm;

    @Enumerated(EnumType.STRING)
    @Column(name = "ItemType", nullable = false)
    private ItemType itemType;

    @Column(name = "ItemIdx", nullable = false)
    private Long itemIdx;

    @Column(name = "BuyDate", nullable = false)
    private LocalDate buyDate;

    @Column(name = "Cnt", nullable = false)
    private int cnt = 0;

    public enum ItemType { SEED, TOOL }

    // ✏️ TODO: increase()
}