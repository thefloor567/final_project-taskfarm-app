package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 코인 거래내역 (tbCoinLedger). 주민판매=EARN, 상점구매=SPEND. 이력성 → CreateDate만.
 */
@Entity
@Table(name = "tbCoinLedger")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbCoinLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Coin")
    private Long idxCoin;

    @Column(name = "Idx_Farm", nullable = false)
    private Long idxFarm;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false)
    private LedgerType type;

    @Column(name = "Amount", nullable = false)
    private int amount;

    @Column(name = "Reason", nullable = false, length = 100)
    private String reason;

    @Column(name = "RefIdx")
    private Long refIdx;

    @CreatedDate
    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createDate;

    public enum LedgerType { EARN, SPEND }
}