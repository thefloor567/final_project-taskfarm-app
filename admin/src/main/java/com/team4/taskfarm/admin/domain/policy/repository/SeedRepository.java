package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.farm.TbSeed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeedRepository extends JpaRepository<TbSeed, Long> {
}
