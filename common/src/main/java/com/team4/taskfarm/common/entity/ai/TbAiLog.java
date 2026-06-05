package com.team4.taskfarm.common.entity.ai;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Gemini 호출/책정 이력 (tbAiLog).
 * IsCache로 캐시 적중률·비용 통계 → 킬러포인트 증명 데이터. 이력성 → CreateDate만.
 */
@Entity
@Table(name = "tbAiLog")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbAiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_AiLog")
    private Long idxAiLog;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

    @Column(name = "Idx_Todo")
    private Long idxTodo;

    @Column(name = "RewardExp", nullable = false)
    private int rewardExp = 0;

    @Column(name = "IsCache", nullable = false)
    private boolean isCache = false;

    @Column(name = "Token")
    private Integer token;

    @CreatedDate
    @Column(name = "CreateDate", updatable = false)
    private LocalDateTime createDate;
}