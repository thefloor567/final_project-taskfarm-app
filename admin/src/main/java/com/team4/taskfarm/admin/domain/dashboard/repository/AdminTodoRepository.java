package com.team4.taskfarm.admin.domain.dashboard.repository;
 
import com.team4.taskfarm.common.entity.todo.TbTodo;
import org.springframework.data.jpa.repository.JpaRepository;
 
public interface AdminTodoRepository extends JpaRepository<TbTodo, Long> {
    long countByIsDoneTrue();
}