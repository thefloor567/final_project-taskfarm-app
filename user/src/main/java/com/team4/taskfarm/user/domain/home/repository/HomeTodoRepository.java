package com.team4.taskfarm.user.domain.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.team4.taskfarm.common.entity.todo.TbTodo;

public interface HomeTodoRepository extends JpaRepository<TbTodo, Long>{
	long countByIdxUserAndDeleteDateIsNull(Long idxUser);
	long countByIdxUserAndIsDoneTrueAndDeleteDateIsNull(Long idxUser);
}
