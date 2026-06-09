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
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobResult;
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
			@RequestParam(required = false) Boolean isDone, // defaultValue = "false" 삭제함 => 전체 조회를 하는데 default 설정 때문에 false인 것만 조회됨
	        @RequestParam(required = false) Long idxCat,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate
			){

		return ok(todoService.getTodoList(uid(), isDone, idxCat, dueDate));
	}
	
	// 할일 단건 조회
	@GetMapping("/{idxTodo}")
	public ResponseEntity<ApiResponse<TodoResponse>> getTodo(@PathVariable Long idxTodo) {

	    return ok(todoService.getTodo(uid(), idxTodo));
	}
	
	// 할일 생성
	@PostMapping
	public ResponseEntity<ApiResponse<TodoResponse>> createTodo(
				@Valid @RequestBody TodoRequest request
			) {

		return ok(todoService.createTodo(uid(), request));
	}
	
	// 할일 수정
	@PatchMapping("/{idxTodo}")
	public ResponseEntity<ApiResponse<TodoResponse>> updateTodo(
				@PathVariable Long idxTodo,
				@Valid @RequestBody TodoRequest request
			) {

		return ok(todoService.updateTodo(uid(), idxTodo, request));
	}
	
	// 할일 삭제
	@DeleteMapping("/{idxTodo}")
	public ResponseEntity<ApiResponse<Void>> deleteTodo(
				@PathVariable Long idxTodo
			){

		todoService.deleteTodo(uid(), idxTodo);
		return ok();
	}
	
	// 할일 완료
	@PatchMapping("/{idxTodo}/complete")
	public ResponseEntity<ApiResponse<TodoResponse>> completeTodo(
				@PathVariable Long idxTodo
			){

		return ok(todoService.completeTodo(uid(), idxTodo));
	}
	
	// 할일 완료 해제
	@PatchMapping("/{idxTodo}/incomplete")
	public ResponseEntity<ApiResponse<TodoResponse>> incompleteTodo(
	        @PathVariable Long idxTodo
	) {

	    return ok(todoService.incompleteTodo(uid(), idxTodo));
	}
	
	// AI 경험치 추천 비동기 접수
	@PostMapping("/{idxTodo}/recommend")
	public ResponseEntity<ApiResponse<AiRecommendJobResult>> requestRecommendExp(
	        @PathVariable Long idxTodo
	) {
	    return ok(todoService.requestRecommendExp(uid(), idxTodo));
	}

	// AI 경험치 추천 작업 결과 조회
	@GetMapping("/recommend/jobs/{jobId}")
	public ResponseEntity<ApiResponse<AiRecommendJobResult>> getRecommendExpJobResult(
	        @PathVariable String jobId
	) {
	    return ok(todoService.getRecommendExpJobResult(jobId));
	}
	
	// 유저 가져오는 메서드
	private Long uid() { Long id = getCurrentUserIdx(); return id;} 
	
}
