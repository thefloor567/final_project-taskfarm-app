package com.team4.taskfarm.user.domain.achievement.repository;

import com.team4.taskfarm.common.entity.social.TbAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbAchievementRepository extends JpaRepository<TbAchievement, Long> {

    /** 활성 업적 전체 (목록 화면·집계용). */
    List<TbAchievement> findByIsActiveTrue();

    /** 특정 집계종류(CondType)의 활성 업적들 — 훅에서 "이 활동에 걸린 업적" 조회용. */
    List<TbAchievement> findByCondTypeAndIsActiveTrue(String condType);

    /** 코드로 단건. */
    Optional<TbAchievement> findByCode(String code);
}