package com.team4.taskfarm.user.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.user.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 유저 찾기 (로그인에 필요)
    User findByEmail(String email);
}