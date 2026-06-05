package com.team4.taskfarm.user.domain.todo.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.todo.dto.TodoRequest;
import com.team4.taskfarm.user.domain.todo.dto.TodoResponse;
import com.team4.taskfarm.user.domain.todo.service.TodoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoApiController extends UserBaseController {
	
	private final TodoService todoService;
	
	// 할일 목록 조회
	@GetMapping
	public ResponseEntity<ApiResponse<List<TodoResponse>>> getTodoList(
			@RequestParam(required = false, defaultValue = "false") Boolean isDone,
	        @RequestParam(required = false) Long idxCat,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
			){
		Long idxUser = getCurrentUserIdx();
		return ok(todoService.getTodoList(idxUser, isDone, idxCat, dueDate));
	}
	
	// 할일 생성
	@PostMapping
	public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
				@Valid @RequestBody TodoRequest request
			) {
		Long idxUser = getCurrentUserIdx();
		return ok(todoService.createTodo(idxUser, request));
	}
	
	// 할일 수정
	@PatchMapping("/{idxTodo}")
	public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
				@PathVariable Long idxTodo,
				@Valid @RequestBody TodoRequest request
			) {
		Long idxUser = getCurrentUserIdx();
		return ok(todoService.updateTodo(idxUser, idxTodo, request));
	}
	
	// 할일 삭제
	@DeleteMapping("/{idxTodo}")
	public ResponseEntity<ApiResponse<Void>> deleteTodo(
				@PathVariable Long idxTodo
			){
		Long idxUser = getCurrentUserIdx();
		todoService.deleteTodo(idxUser, idxTodo);
		return ok();
	}
	
	// 할일 완료
	@PatchMapping("/{idxTodo}/complete")
	public ResponseEntity<ApiResponse<TodoResponse>> completeTodo(
				@PathVariable Long idxTodo
			){
		Long idxUser = getCurrentUserIdx();
		return ok(todoService.completeTodo(idxUser, idxTodo));
	}
}
