package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbFarm;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TbFarmRepository extends JpaRepository<TbFarm, Long> {

    /** 유저당 농장 1:1. 신규 유저는 비어 있을 수 있어 Optional. */
    Optional<TbFarm> findByIdxUser(Long idxUser);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM TbFarm f WHERE f.idxUser = :idxUser")
    Optional<TbFarm> findByIdxUserForUpdate(@Param("idxUser") Long idxUser);
}