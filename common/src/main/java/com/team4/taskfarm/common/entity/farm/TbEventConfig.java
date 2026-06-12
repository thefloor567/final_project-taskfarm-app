package com.team4.taskfarm.common.entity.farm;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 이벤트 정책 (tbEventConfig). 스트릭별 풀/확률. 어드민 밸런스 조정. UpdateDate만.
 */
@Entity
@Table(name = "tbEventConfig")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbEventConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_EventConfig")
    private Long idxEventConfig;

    @Column(name = "StreakMin", nullable = false)
    private int streakMin;

    @Column(name = "EventKey", nullable = false, length = 30)
    private String eventKey;

    // 농장확장: 위협 이벤트의 타격 범위 (none=전역보너스, one/multi/all=대상 밭)
    @Enumerated(EnumType.STRING)
    @Column(name = "Scope", nullable = false)
    private Scope scope = Scope.none;

    @Column(name = "ScopeCount", nullable = false)
    private int scopeCount = 1;   // multi일 때 대상 밭 수

    @Column(name = "Weight", nullable = false)
    private int weight = 1;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @LastModifiedDate
    @Column(name = "UpdateDate")
    private LocalDateTime updateDate;

    public enum Scope { none, one, multi, all }

    /** 어드민 이벤트 정책 수정 (가중치/활성여부). */
    public void updatePolicy(int weight, boolean isActive) {
        this.weight = weight;
        this.isActive = isActive;
    }

    /** 농장확장: 타격 범위 수정 (어드민에서 scope 조정 시). */
    public void updateScope(Scope scope, int scopeCount) {
        this.scope = scope;
        this.scopeCount = scopeCount;
    }
}