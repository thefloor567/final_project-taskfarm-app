package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbSeedInv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TbSeedInvRepository extends JpaRepository<TbSeedInv, Long> {

    /** 보유 씨앗 목록 (심을 씨앗 고르기 UI용) */
    List<TbSeedInv> findByIdxFarm(Long idxFarm);

    /** 특정 씨앗 보유분 (심기 시 차감 대상) */
    Optional<TbSeedInv> findByIdxFarmAndIdxSeed(Long idxFarm, Long idxSeed);
}