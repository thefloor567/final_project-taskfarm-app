package com.team4.taskfarm.user.domain.todo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.todo.TbTodo;

public interface TodoRepository extends JpaRepository<TbTodo, Long>{

	Optional<TbTodo> findByIdxUserAndIdxTodo(Long idxUser, Long idxTodo);

	List<TbTodo> findByIdxUserAndIsDoneAndIdxCatAndDueDateBetweenOrderByCreateDateDesc(Long idxUser, Boolean isDone,
			Long idxCat, LocalDateTime start, LocalDateTime end);
	
	List<TbTodo> findByIdxUserAndIsDoneAndDueDateBetweenOrderByCreateDateDesc(Long idxUser, Boolean isDone,
			LocalDateTime start, LocalDateTime end);

	List<TbTodo> findByIdxUserAndIdxCatAndDueDateBetweenOrderByCreateDateDesc(Long idxUser, Long idxCat,
			LocalDateTime start, LocalDateTime end);

	List<TbTodo> findByIdxUserAndDueDateBetweenOrderByCreateDateDesc(Long idxUser, LocalDateTime start,
			LocalDateTime end);
	
	List<TbTodo> findByIdxUserAndIsDoneAndIdxCatOrderByCreateDateDesc(Long idxUser, Boolean isDone, Long idxCat);

	List<TbTodo> findByIdxUserAndIsDoneOrderByCreateDateDesc(Long idxUser, Boolean isDone);

	List<TbTodo> findByIdxUserAndIdxCatOrderByCreateDateDesc(Long idxUser, Long idxCat);

	List<TbTodo> findByIdxUserOrderByCreateDateDesc(Long idxUser);
}
