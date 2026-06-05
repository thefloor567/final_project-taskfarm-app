package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 농장에서 유저 레벨/정보 조회용 (읽기 전용 목적).
 */
public interface TbUserRepository extends JpaRepository<TbUser, Long> {
}