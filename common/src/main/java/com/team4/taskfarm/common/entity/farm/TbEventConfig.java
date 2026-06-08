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

    @Column(name = "Weight", nullable = false)
    private int weight = 1;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @LastModifiedDate
    @Column(name = "UpdateDate")
    private LocalDateTime updateDate;

    /** 어드민 이벤트 정책 수정 (가중치/활성여부). */
    public void updatePolicy(int weight, boolean isActive) {
        this.weight = weight;
        this.isActive = isActive;
    }
}