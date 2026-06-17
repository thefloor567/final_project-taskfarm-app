package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbTool;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface TbToolRepository extends JpaRepository<TbTool, Long> {

    /** 상점 진열용 — 판매중인 도구만 (Idx 순) */
    List<TbTool> findByIsActiveTrueOrderByIdxToolAsc();
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TbTool t WHERE t.idxTool = :toolId")
    Optional<TbTool> findByIdForUpdate(@Param("toolId") Long toolId);
}