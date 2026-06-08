package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.farm.TbEventConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventConfigRepository extends JpaRepository<TbEventConfig, Long> {
    List<TbEventConfig> findAllByOrderByStreakMinAscIdxEventConfigAsc();
}
