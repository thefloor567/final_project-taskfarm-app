package com.team4.taskfarm.user.domain.farm.repository;

import com.team4.taskfarm.common.entity.farm.TbFarmEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface TbFarmEventRepository extends JpaRepository<TbFarmEvent, Long> {

    /** (유저+날짜) 유니크. 같은 날 이벤트는 하나. */
    Optional<TbFarmEvent> findByIdxUserAndEventDate(Long idxUser, LocalDate eventDate);
}