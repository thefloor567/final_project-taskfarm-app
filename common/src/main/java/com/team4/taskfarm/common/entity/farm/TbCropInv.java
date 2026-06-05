package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 수확 작물 인벤토리 (tbCropInv). 주민 주문 이행 재료. 작물=씨앗 종류로 식별. UpdateDate만.
 */
@Entity
@Table(name = "tbCropInv")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbCropInv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_CropInv")
    private Long idxCropInv;

    @Column(name = "Idx_Farm", nullable = false)
    private Long idxFarm;

    @Column(name = "Idx_Seed", nullable = false)
    private Long idxSeed;

    @Column(name = "Qty", nullable = false)
    private int qty = 0;

    @LastModifiedDate
    @Column(name = "UpdateDate")
    private LocalDateTime updateDate;
    
    
    /** 신규 작물 인벤토리 생성 */
    public static TbCropInv create(Long idxFarm, Long idxSeed, int qty) {
        TbCropInv inv = new TbCropInv();
        inv.idxFarm = idxFarm;
        inv.idxSeed = idxSeed;
        inv.qty = qty;
        return inv;
    }

    /** 수확량 적립 */
    public void add(int amount) {
        this.qty += amount;
    }
    
    /** 작물 차감(주문 납품). 부족하면 예외. */
    public void consume(int amount) {
        if (qty < amount) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("작물이 부족합니다.");
        }
        qty -= amount;
    }
}