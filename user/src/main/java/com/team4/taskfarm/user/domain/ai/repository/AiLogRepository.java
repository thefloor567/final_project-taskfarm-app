package com.team4.taskfarm.user.domain.ai.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.ai.TbAiLog;

public interface AiLogRepository extends JpaRepository<TbAiLog, Long>{

	Optional<TbAiLog> findTopByIdxUserAndIdxTodoOrderByCreateDateDesc(Long idxUser, Long idxTodo);
	
	List<TbAiLog> findByIdxUserOrderByCreateDateAsc(Long idxUser);
	
	long countByIdxUser(Long idxUser);
}
