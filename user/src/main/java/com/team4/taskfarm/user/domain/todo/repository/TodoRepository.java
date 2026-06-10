package com.team4.taskfarm.user.domain.todo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.todo.TbTodo;

public interface TodoRepository extends JpaRepository<TbTodo, Long>, TodoRepositoryCustom{

	Optional<TbTodo> findByIdxUserAndIdxTodoAndDeleteDateIsNull(Long idxUser, Long idxTodo);
}
