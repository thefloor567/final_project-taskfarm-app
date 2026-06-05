package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbCropInv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface TbCropInvRepository extends JpaRepository<TbCropInv, Long> {

    /** 농장의 수확 작물 인벤토리 전체 */
    List<TbCropInv> findByIdxFarm(Long idxFarm);
    
    /** 수확 적립 시 같은 작물 보유분 조회 */
    Optional<TbCropInv> findByIdxFarmAndIdxSeed(Long idxFarm, Long idxSeed);
}