package com.team4.taskfarm.user.domain.todo.dto;

import java.time.LocalDateTime;

import com.team4.taskfarm.common.entity.todo.TbTodo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoRequest {
	
	private Long idxCat;
	
	@NotBlank(message = "할 일 제목은 필수로 작성해야 합니다.")
	private String title;
	
	private String content;
	
	private TbTodo.Priority priority;
	
	private LocalDateTime dueDate;
	
	// Request DTO 만들어주는 메서드
	public static TodoRequest of(Long idxCat, String title, String content, TbTodo.Priority priority, LocalDateTime dueDate) {
		return new TodoRequest(idxCat, title, content, priority, dueDate);
	}
	
	// 카테고리가 없는 Request DTO 만들어주는 메서드
	public static TodoRequest of(String title, String content, TbTodo.Priority priority, LocalDateTime dueDate) {
		return new TodoRequest(null, title, content, priority, dueDate);
	}

}
