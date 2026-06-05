package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbTool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TbToolRepository extends JpaRepository<TbTool, Long> {

    /** 상점 진열용 — 판매중인 도구만 (Idx 순) */
    List<TbTool> findByIsActiveTrueOrderByIdxToolAsc();
}