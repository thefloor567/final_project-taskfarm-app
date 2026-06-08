package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.farm.TbTool;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToolRepository extends JpaRepository<TbTool, Long> {
    List<TbTool> findAllByOrderByIdxToolAsc();
}
