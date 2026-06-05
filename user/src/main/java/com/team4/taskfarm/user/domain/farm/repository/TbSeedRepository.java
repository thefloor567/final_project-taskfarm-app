package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbSeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbSeedRepository extends JpaRepository<TbSeed, Long> {

    /** 작물 렌더 시 씨앗 이름을 한 번에 가져오기 (N+1 방지) */
    List<TbSeed> findByIdxSeedIn(List<Long> idxSeeds);
    
    /** 상점 진열용 — 판매중인 씨앗만 (Idx 순) */
    List<TbSeed> findByIsActiveTrueOrderByIdxSeedAsc();
}