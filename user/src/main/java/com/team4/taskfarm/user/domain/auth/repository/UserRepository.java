package com.team4.taskfarm.user.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.user.TbUser;
import java.util.Optional;

public interface UserRepository extends JpaRepository<TbUser, Long> {
    // 이메일로 유저 찾기 (로그인에 필요)
	Optional<TbUser> findByEmail(String email);
}