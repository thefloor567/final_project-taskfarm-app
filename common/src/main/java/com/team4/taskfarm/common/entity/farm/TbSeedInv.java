package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 보유 씨앗 인벤토리 (tbSeedInv). 구매했지만 안 심은 씨앗. 농장·씨앗 조합 유일(UK). UpdateDate만.
 */
@Entity
@Table(name = "tbSeedInv")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbSeedInv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_SeedInv")
    private Long idxSeedInv;

    @Column(name = "Idx_Farm", nullable = false)
    private Long idxFarm;

    @Column(name = "Idx_Seed", nullable = false)
    private Long idxSeed;

    @Column(name = "Qty", nullable = false)
    private int qty = 0;

    @LastModifiedDate
    @Column(name = "UpdateDate")
    private LocalDateTime updateDate;
}