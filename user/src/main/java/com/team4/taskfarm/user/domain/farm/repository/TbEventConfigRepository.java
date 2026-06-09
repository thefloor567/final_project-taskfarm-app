package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbEventConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbEventConfigRepository extends JpaRepository<TbEventConfig, Long> {

    /**
     * 해당 스트릭 이상에서 활성인 이벤트 풀.
     * StreakMin <= 유저스트릭 인 것들 중 활성. (예: streak 5면 StreakMin 0,3,5 풀이 다 후보)
     */
    List<TbEventConfig> findByIsActiveTrueAndStreakMinLessThanEqual(int streak);
}