package com.team4.taskfarm.common.entity.farm;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 씨앗 마스터 (tbSeed). 상점 상품. 가격/보상/재고/한도 어드민 조정. 코드 유일(UK).
 */
@Entity
@Table(name = "tbSeed")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbSeed extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Seed")
    private Long idxSeed;

    @Column(name = "Code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Waters", nullable = false)
    private int waters;

    @Column(name = "Price", nullable = false)
    private int price;

    @Column(name = "Reward", nullable = false)
    private int reward;

    @Column(name = "Stock", nullable = false)
    private int stock;

    @Column(name = "DailyLimit", nullable = false)
    private int dailyLimit;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    // ✏️ TODO: 어드민 수정 메서드
}