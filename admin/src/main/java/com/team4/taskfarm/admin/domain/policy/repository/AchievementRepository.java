package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.social.TbAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AchievementRepository extends JpaRepository<TbAchievement, Long> {
    List<TbAchievement> findAllByOrderByIdxAchievementAsc();
}
