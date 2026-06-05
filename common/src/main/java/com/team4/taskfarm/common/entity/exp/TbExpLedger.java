package com.team4.taskfarm.common.entity.exp;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 경험치 거래내역 (tbExpLedger). 적립/차감 이력.
 * 잔고는 tbUser.Exp(캐시), 이력은 여기. UpdateDate 없는 이력성 → CreateDate만.
 */
@Entity
@Table(name = "tbExpLedger")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbExpLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Exp")
    private Long idxExp;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

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