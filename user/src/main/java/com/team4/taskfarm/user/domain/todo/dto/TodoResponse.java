package com.team4.taskfarm.user.domain.todo.dto;

import java.time.LocalDateTime;

import com.team4.taskfarm.common.entity.todo.TbTodo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TodoResponse {
	
	private Long idxTodo;
	private Long idxUser;
	private Long idxCat;
	
	private String title;
	private String content;
	private TbTodo.Priority priority;
	
	// 카테고리 이름을 출력하기 위해 사용
	private String categoryName;
	
	private boolean isDone;
	
	private LocalDateTime dueDate;
	private LocalDateTime completeDate;
	private LocalDateTime createDate;
	private LocalDateTime updateDate;
	
	// DB에서 가져온 TbTodo 엔티티를 TodoResponse DTO로 바꿔주는 메서드
	public static TodoResponse from(TbTodo todo, String categoryName) {
		return TodoResponse.builder()
						.idxTodo(todo.getIdxTodo())
						.idxUser(todo.getIdxUser())
						.idxCat(todo.getIdxCat())
						.title(todo.getTitle())
						.content(todo.getContent())
						.priority(todo.getPriority())
						.categoryName(categoryName)
						.isDone(todo.isDone())
						.dueDate(todo.getDueDate())
						.completeDate(todo.getCompleteDate())
						.createDate(todo.getCreateDate())
						.updateDate(todo.getUpdateDate())
						.build();
	}
	
}
