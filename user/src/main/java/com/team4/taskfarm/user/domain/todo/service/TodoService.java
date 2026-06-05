package com.team4.taskfarm.user.domain.todo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.todo.dto.TodoRequest;
import com.team4.taskfarm.user.domain.todo.dto.TodoResponse;
import com.team4.taskfarm.user.domain.todo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TodoService {
	
	private final TodoRepository todoRepository;
	
	// 할일 가져오기
	@Transactional(readOnly = true)
	public List<TodoResponse> getTodoList(Long idxUser, Boolean isDone, Long idxCat, LocalDate dueDate){
		
		List<TbTodo> todo;
		
		if(dueDate != null) {
			LocalDateTime start = dueDate.atStartOfDay();
			LocalDateTime end = dueDate.plusDays(1).atStartOfDay();
			
			// 1. 완료 여부 + 카테고리 + 날짜
			if (isDone != null && idxCat != null) {
				todo = todoRepository.findByIdxUserAndIsDoneAndIdxCatAndDueDateBetweenOrderByCreateDateDesc(
							idxUser, isDone, idxCat, start, end
						);
				
			// 2. 완려 여부 + 날짜
			} else if (isDone != null) {
				todo = todoRepository.findByIdxUserAndIsDoneAndDueDateBetweenOrderByCreateDateDesc(
							idxUser, isDone, start, end
						);
				
			// 3. 카테고리 + 날짜
			} else if (idxCat != null) {
				todo = todoRepository.findByIdxUserAndIdxCatAndDueDateBetweenOrderByCreateDateDesc(
							idxUser, idxCat, start, end
						);
			
			// 4. 날짜
			} else {
				todo = todoRepository.findByIdxUserAndDueDateBetweenOrderByCreateDateDesc(
							idxUser, start, end
						);
			}
		} else {
			// 5. 완려 여부 + 카테고리
			if (isDone != null && idxCat != null) {
				todo = todoRepository.findByIdxUserAndIsDoneAndIdxCatOrderByCreateDateDesc(
							idxUser, isDone, idxCat
						);
			// 6. 완려 여부
			} else if (isDone != null) {
				todo = todoRepository.findByIdxUserAndIsDoneOrderByCreateDateDesc(
							idxUser, isDone
						);
			// 7. 카테고리
			} else  if (idxCat != null) {
				todo = todoRepository.findByIdxUserAndIdxCatOrderByCreateDateDesc(
							idxUser, idxCat
						);
			// 8. 모두 X
			} else {
				todo = todoRepository.findByIdxUserOrderByCreateDateDesc(idxUser);
			}
		}
		
		return todo.stream()
					.map(TodoResponse::from)
					.toList();
	}
	
	// 할일 생성 (엔티티로 바꿔서 repo에 저장)
	@Transactional
	public TodoResponse createTodo(Long idxUser, TodoRequest request) {
		TbTodo todo = TbTodo.of(
		        idxUser,
		        request.getIdxCat(),
		        request.getTitle(),
		        request.getContent(),
		        request.getPriority(),
		        request.getDueDate()
		);
		
		TbTodo savedTodo = todoRepository.save(todo);
		return TodoResponse.from(savedTodo);
	}
	
	// findTodo에서 찾아오고 업데이트
	@Transactional
	public TodoResponse updateTodo(Long idxUser, Long idxTodo, TodoRequest request) {
		TbTodo todo = findTodo(idxUser, idxTodo);
		todo.update(request.getIdxCat(), request.getTitle(), request.getContent(), request.getPriority(), request.getDueDate());
		return TodoResponse.from(todo);
	}
	
	// findTodo에서 찾아오고 지우기
	@Transactional
	public void deleteTodo(Long idxUser, Long idxTodo) {
		TbTodo todo = findTodo(idxUser, idxTodo);
		todoRepository.delete(todo);
	}
	
	// findTodo에서 찾아오고 완료 처리
	@Transactional
	public TodoResponse completeTodo(Long idxUser, Long idxTodo) {
		// 완료 시 xp 적립 + 물방울 소량 지급 이거 구현 아직 안함
		TbTodo todo = findTodo(idxUser, idxTodo);
		todo.complete();
		return TodoResponse.from(todo);
	}
	
	// repo에서 idxUser와 idxTodo로 해당 사용자의 할일 찾아오기
	private TbTodo findTodo(Long idxUser, Long idxTodo) {
		return todoRepository.findByIdxUserAndIdxTodo(idxUser, idxTodo)
								.orElseThrow(() -> CustomException.notFound("할일을 찾을 수 없습니다."));
	}
	
}
