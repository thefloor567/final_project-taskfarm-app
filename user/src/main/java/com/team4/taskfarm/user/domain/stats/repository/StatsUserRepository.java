package com.team4.taskfarm.user.domain.stats.repository;

import com.team4.taskfarm.common.entity.user.TbUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatsUserRepository extends JpaRepository<TbUser, Long> {

    Optional<TbUser> findByIdxUserAndDeleteDateIsNull(Long idxUser);
}
