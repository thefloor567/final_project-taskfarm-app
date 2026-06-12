package com.team4.taskfarm.common.entity.social;

import com.team4.taskfarm.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 유저 업적 달성기록 (tbUserAchievement).
 * - (Idx_User, Idx_Achievement) UNIQUE 로 중복 달성·중복 지급 방지(멱등 핵심).
 * - 조건 충족 시 1행 insert + 코인 지급 + 칭호 해금을 한 트랜잭션으로.
 */
@Entity
@Table(name = "tbUserAchievement")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TbUserAchievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Idx_UserAch")
    private Long idxUserAch;

    @Column(name = "Idx_User", nullable = false)
    private Long idxUser;

    @Column(name = "Idx_Achievement", nullable = false)
    private Long idxAchievement;

    @Column(name = "AchieveDate")
    private LocalDateTime achieveDate;

    /** 달성 기록 생성. 달성 시각을 현재로 찍는다. */
    public static TbUserAchievement of(Long idxUser, Long idxAchievement) {
        TbUserAchievement ua = new TbUserAchievement();
        ua.idxUser = idxUser;
        ua.idxAchievement = idxAchievement;
        ua.achieveDate = LocalDateTime.now();
        return ua;
    }
}