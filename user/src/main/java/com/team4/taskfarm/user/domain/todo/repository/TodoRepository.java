package com.team4.taskfarm.user.domain.todo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.todo.TbTodo;

public interface TodoRepository extends JpaRepository<TbTodo, Long>{

	List<TbTodo> findByIdxUserAndIsDoneAndIdxCatAndDueDateBetweenAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, Boolean isDone, Long idxCat, LocalDateTime start, LocalDateTime end
	);

	List<TbTodo> findByIdxUserAndIsDoneAndDueDateBetweenAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, Boolean isDone, LocalDateTime start, LocalDateTime end
	);

	List<TbTodo> findByIdxUserAndIdxCatAndDueDateBetweenAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, Long idxCat, LocalDateTime start, LocalDateTime end
	);

	List<TbTodo> findByIdxUserAndDueDateBetweenAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, LocalDateTime start, LocalDateTime end
	);

	List<TbTodo> findByIdxUserAndIsDoneAndIdxCatAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, Boolean isDone, Long idxCat
	);

	List<TbTodo> findByIdxUserAndIsDoneAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, Boolean isDone
	);

	List<TbTodo> findByIdxUserAndIdxCatAndDeleteDateIsNullOrderByCreateDateDesc(
	        Long idxUser, Long idxCat
	);

	List<TbTodo> findByIdxUserAndDeleteDateIsNullOrderByCreateDateDesc(Long idxUser);

	Optional<TbTodo> findByIdxUserAndIdxTodoAndDeleteDateIsNull(Long idxUser, Long idxTodo);

	Optional<TbTodo> findByIdxTodoAndIdxUserAndDeleteDateIsNull(Long idxTodo, Long idxUser);
}
