package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.farm.TbTool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToolRepository extends JpaRepository<TbTool, Long> {
}
