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

    /** 어드민 상점 정책 수정 (가격/보상/재고/하루한도). */
    public void updatePolicy(int price, int reward, int stock, int dailyLimit) {
        this.price = price;
        this.reward = reward;
        this.stock = stock;
        this.dailyLimit = dailyLimit;
    }


    /** 구매 가능 여부 검증 (판매중 + 재고). 수량만큼 재고 차감. */
    public void purchase(int qty) {
        if (!isActive) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("현재 판매하지 않는 씨앗입니다.");
        }
        if (qty <= 0) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("구매 수량이 올바르지 않습니다.");
        }
        if (stock < qty) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("재고가 부족합니다.");
        }
        stock -= qty;
    }
}