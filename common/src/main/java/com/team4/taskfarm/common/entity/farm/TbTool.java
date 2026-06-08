package com.team4.taskfarm.common.entity.farm;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도구 마스터 (tbTool). 허수아비/비료/온실. 어드민 밸런스 조정. 코드 유일(UK).
 */
@Entity
@Table(name = "tbTool")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbTool extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Tool")
    private Long idxTool;

    @Column(name = "Code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false)
    private ToolType type;

    @Column(name = "Uses", nullable = false)
    private int uses = 1;

    @Column(name = "Price", nullable = false)
    private int price;

    @Column(name = "Stock", nullable = false)
    private int stock;

    @Column(name = "DailyLimit", nullable = false)
    private int dailyLimit;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    public enum ToolType { scarecrow, fertilizer, greenhouse }

    /** 어드민 상점 정책 수정 (가격/효과수치/재고/하루한도). */
    public void updatePolicy(int price, int uses, int stock, int dailyLimit) {
        this.price = price;
        this.uses = uses;
        this.stock = stock;
        this.dailyLimit = dailyLimit;
    }

    /** 구매 가능 여부 검증(판매중 + 재고) + 재고 1 차감. */
    public void purchase() {
        if (!isActive) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("현재 판매하지 않는 도구입니다.");
        }
        if (stock < 1) {
            throw com.team4.taskfarm.common.exception.CustomException
                    .badRequest("재고가 부족합니다.");
        }
        stock -= 1;
    }
}