package com.team4.taskfarm.user.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.user.TbUser;
import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<TbUser, Long> {

    // 이메일로 유저 찾기 (로그인에 필요)
    Optional<TbUser> findByEmail(String email);

    // 친구코드로 유저 찾기 (친구 신청에 필요)
    Optional<TbUser> findByFriendCode(String friendCode);
}