package com.team4.taskfarm.user.domain.home.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.farm.TbFarm;


public interface HomeFarmRepository extends JpaRepository<TbFarm, Long>{
	Optional<TbFarm> findByIdxUser(Long idxUser);
}
