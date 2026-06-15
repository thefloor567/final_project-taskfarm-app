package com.team4.taskfarm.user.domain.achievement.repository;

import com.team4.taskfarm.common.entity.social.TbUserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbUserAchievementRepository extends JpaRepository<TbUserAchievement, Long> {

    /** 이 유저가 이미 달성했는지 (멱등 체크용). */
    boolean existsByIdxUserAndIdxAchievement(Long idxUser, Long idxAchievement);

    /** 이 유저의 달성기록 전체 (목록 화면에서 달성 여부 표시용). */
    List<TbUserAchievement> findByIdxUser(Long idxUser);

    /** 이 유저의 달성 개수 (요약·friend_count 등 자체 카운트엔 안 씀, 통계용). */
    long countByIdxUser(Long idxUser);
}