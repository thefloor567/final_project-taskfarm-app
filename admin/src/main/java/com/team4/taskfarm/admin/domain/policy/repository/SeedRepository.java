package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.farm.TbSeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeedRepository extends JpaRepository<TbSeed, Long> {
    List<TbSeed> findAllByOrderByIdxSeedAsc();
}
