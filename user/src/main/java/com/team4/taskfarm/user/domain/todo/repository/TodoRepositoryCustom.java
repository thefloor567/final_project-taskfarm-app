package com.team4.taskfarm.user.domain.todo.repository;

import com.team4.taskfarm.common.entity.todo.TbTodo;
import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepositoryCustom {
	List<TbTodo> search(Long idxUser, Boolean isDone, Long idxCat, LocalDateTime start, LocalDateTime end);
}
