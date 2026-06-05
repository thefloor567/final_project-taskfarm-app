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
    
    
    /** 씨앗 1개 소비(심기). 재고 없으면 예외. */
    public void consumeOne() {
        if (qty <= 0) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("보유한 씨앗이 없습니다.");
        }
        qty--;
    }
    
    /** 신규 보유 씨앗 생성 */
    public static TbSeedInv create(Long idxFarm, Long idxSeed, int qty) {
        TbSeedInv inv = new TbSeedInv();
        inv.idxFarm = idxFarm;
        inv.idxSeed = idxSeed;
        inv.qty = qty;
        return inv;
    }

    /** 수량 적립(구매) */
    public void add(int amount) {
        if (amount > 0) this.qty += amount;
    }
}