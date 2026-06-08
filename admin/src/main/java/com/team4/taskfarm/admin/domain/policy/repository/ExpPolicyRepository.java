package com.team4.taskfarm.admin.domain.policy.repository;

import com.team4.taskfarm.common.entity.exp.TbExpPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpPolicyRepository extends JpaRepository<TbExpPolicy, Long> {
    List<TbExpPolicy> findAllByOrderByPriorityAsc();
}
