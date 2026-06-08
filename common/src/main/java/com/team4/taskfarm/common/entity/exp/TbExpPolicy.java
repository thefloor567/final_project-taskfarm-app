package com.team4.taskfarm.common.entity.exp;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경험치 정책 (tbExpPolicy). 마스터데이터 — 어드민 무중단 조정. 우선순위당 1정책(UK).
 */
@Entity
@Table(name = "tbExpPolicy")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbExpPolicy extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_ExpPolicy")
    private Long idxExpPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "Priority", nullable = false, unique = true)
    private Priority priority;

    @Column(name = "BaseExp", nullable = false)
    private int baseExp = 10;

    @Column(name = "DoneDrops", nullable = false)
    private int doneDrops = 1;

    @Column(name = "LevelUpDrops", nullable = false)
    private int levelUpDrops = 5;

    public enum Priority { A, B, C }

    public void update(int baseExp, int doneDrops, int levelUpDrops) {
        this.baseExp = baseExp;
        this.doneDrops = doneDrops;
        this.levelUpDrops = levelUpDrops;
    }
}