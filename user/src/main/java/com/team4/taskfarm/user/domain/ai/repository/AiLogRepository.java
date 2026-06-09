package com.team4.taskfarm.user.domain.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.ai.TbAiLog;
import com.team4.taskfarm.user.domain.category.entity.Category;

public interface AiLogRepository extends JpaRepository<TbAiLog, Long>{

	Optional<TbAiLog> findTopByIdxUserAndIdxTodoOrderByCreateDateDesc(Long idxUser, Long idxTodo);
	
}
