package com.team4.taskfarm.common.entity.social;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 업적 마스터 엔티티 (tbAchievement).
 * - 콘텐츠(레벨디자인) 성격. 어드민에서 활성/비활성·밸런스 조정.
 * - CondType은 집계 출처(명세 3-5표)와 정확히 일치시킬 것.
 * - 보상은 코인만 (물방울 불가).
 */
@Entity
@Table(name = "tbAchievement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbAchievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_Achievement")
    private Long idxAchievement;

    @Column(name = "Code", nullable = false, length = 40)
    private String code;     // 예: "todo_100"

    @Column(name = "Name", nullable = false, length = 50)
    private String name;

    @Column(name = "Title", nullable = false, length = 50)
    private String title;    // 획득 칭호 (이모지 포함)

    @Enumerated(EnumType.STRING)
    @Column(name = "Category", nullable = false)
    private Category category;

    @Column(name = "CondType", nullable = false, length = 30)
    private String condType;  // 집계 종류 예: "todo_done_total"

    @Column(name = "CondValue", nullable = false)
    private int condValue;    // 목표치

    @Column(name = "RewardCoin", nullable = false)
    private int rewardCoin = 0;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    public enum Category { todo, farm, event, ai, social }

    /** 어드민 밸런스 조정 (목표치·보상·활성). */
    public void updatePolicy(int condValue, int rewardCoin, boolean isActive) {
        this.condValue = condValue;
        this.rewardCoin = rewardCoin;
        this.isActive = isActive;
    }
}