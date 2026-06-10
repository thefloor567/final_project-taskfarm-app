package com.team4.taskfarm.user.domain.user.repository;

import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<TbUser, Long> {
	
}
