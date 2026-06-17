package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbCropInv;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface TbCropInvRepository extends JpaRepository<TbCropInv, Long> {

    /** 농장의 수확 작물 인벤토리 전체 */
    List<TbCropInv> findByIdxFarm(Long idxFarm);
    
    /** 수확 적립 시 같은 작물 보유분 조회 */
    Optional<TbCropInv> findByIdxFarmAndIdxSeed(Long idxFarm, Long idxSeed);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM TbCropInv c WHERE c.idxFarm = :idxFarm AND c.idxSeed = :idxSeed")
    Optional<TbCropInv> findByIdxFarmAndIdxSeedForUpdate(@Param("idxFarm") Long idxFarm, @Param("idxSeed") Long idxSeed);
}