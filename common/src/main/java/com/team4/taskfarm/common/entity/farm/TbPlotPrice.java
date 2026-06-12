package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 밭 슬롯 가격표 (tbPlotPrice) — 마스터 데이터.
 * - 기본 6밭 무료, 7번째부터 레벨+코인 해금.
 * - PK가 Slot(자연키, 자동증가 아님)이라 BaseEntity 대신 createDate 직접 보유.
 *   (BaseEntity는 IDENTITY PK 엔티티들과 패턴이 같아 여기선 미상속)
 *
 * ※ 설계서상 "코드 상수로 대체 가능". 운영 중 가격 조정이 필요 없으면
 *   이 테이블 대신 enum/상수로 둬도 됨. 어드민에서 조정하려면 테이블 권장.
 */
@Entity
@Table(name = "tbPlotPrice")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbPlotPrice {

    @Id
    @Column(name = "Slot")
    private Integer slot;        // 밭 번호 (7, 8, 9...) — 자연키

    @Column(name = "UnlockLevel", nullable = false)
    private int unlockLevel;

    @Column(name = "Price", nullable = false)
    private int price;

    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createDate;

    public static TbPlotPrice of(int slot, int unlockLevel, int price) {
        TbPlotPrice p = new TbPlotPrice();
        p.slot = slot;
        p.unlockLevel = unlockLevel;
        p.price = price;
        p.createDate = LocalDateTime.now();
        return p;
    }
}