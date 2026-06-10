package com.team4.taskfarm.user.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.user.TbUser;

public interface HomeUserRepository extends JpaRepository<TbUser, Long>{

}
