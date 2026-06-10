package com.team4.taskfarm.user.domain.todo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.common.entity.category.TbCategory;
import com.team4.taskfarm.user.domain.category.repository.CategoryRepository;
import com.team4.taskfarm.user.domain.todo.dto.TodoRequest;
import com.team4.taskfarm.user.domain.todo.dto.TodoResponse;
import com.team4.taskfarm.user.domain.todo.repository.TodoRepository;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class TodoService {
	
	private final TodoRepository todoRepository;
	
	// 카테고리 이름 조회용
	private final CategoryRepository categoryRepository;
	
	private final RewardService rewardService;
	
	// 할일 가져오기
	@Transactional(readOnly = true)
	public List<TodoResponse> getTodoList(Long idxUser, Boolean isDone, Long idxCat, LocalDate dueDate){
		
		LocalDateTime start = (dueDate != null) ? dueDate.atStartOfDay() : null;
		LocalDateTime end = (dueDate != null) ? dueDate.plusDays(1).atStartOfDay() : null;
		
		List<TbTodo> todo = todoRepository.search(idxUser, isDone, idxCat, start, end);
		
		// 현재 유저의 카테고리 목록을 한 번만 조회해서
        // idxCat -> categoryName 형태의 Map으로 변환한다.
        // Todo마다 categoryRepository를 호출하면 N+1처럼 조회가 많아질 수 있어서 이 방식이 더 낫다.
        Map<Long, String> categoryNameMap = categoryRepository.findByIdxUserAndDeleteDateIsNull(idxUser)
                .stream()
                .collect(Collectors.toMap(TbCategory::getIdxCat, TbCategory::getName));

        // TodoResponse에 categoryName까지 포함해서 반환한다.
        return todo.stream()
                .map(t -> TodoResponse.from(
                		t,
                		getCategoryNameFromMap(t.getIdxCat(), categoryNameMap)))
                .toList();
    }
	
	// 단건 조회
	@Transactional(readOnly = true)
	public TodoResponse getTodo(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);
	    String categoryName = getCategoryName(idxUser, todo.getIdxCat());
	    return TodoResponse.from(todo, categoryName);
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
		String categoryName = getCategoryName(idxUser, savedTodo.getIdxCat());
		return TodoResponse.from(savedTodo, categoryName);
	}
	
	// findTodo에서 찾아오고 업데이트
	@Transactional
	public TodoResponse updateTodo(Long idxUser, Long idxTodo, TodoRequest request) {
		TbTodo todo = findTodo(idxUser, idxTodo);
		todo.update(request.getIdxCat(), request.getTitle(), request.getContent(), request.getPriority(), request.getDueDate());
		String categoryName = getCategoryName(idxUser, todo.getIdxCat());
		return TodoResponse.from(todo, categoryName);
	}
	
	// findTodo에서 찾아오고 지우기
	@Transactional
	public void deleteTodo(Long idxUser, Long idxTodo) {
		TbTodo todo = findTodo(idxUser, idxTodo);
		todo.softDelete();
	}
	
	// findTodo에서 찾아오고 완료 처리
	@Transactional
    public TodoResponse completeTodo(Long idxUser, Long idxTodo) {
        TbTodo todo = findTodo(idxUser, idxTodo);

        boolean wasDone = todo.isDone();
        todo.complete();
        if (!wasDone) {   // 원래 완료 아니었을 때만 지급 (중복 적립 방지)
            rewardService.grantTodoDone(idxUser, todo.getPriority(), todo.getIdxTodo());
        }

        String categoryName = getCategoryName(idxUser, todo.getIdxCat());
        return TodoResponse.from(todo, categoryName);
    }
	
	// 할일 완료 해제
	@Transactional
    public TodoResponse incompleteTodo(Long idxUser, Long idxTodo) {
        TbTodo todo = findTodo(idxUser, idxTodo);
        todo.incomplete();   // completeDate = null 처리 (이미 구현됨)
        String categoryName = getCategoryName(idxUser, todo.getIdxCat());
        return TodoResponse.from(todo, categoryName);
    }
	
	// repo에서 idxUser와 idxTodo로 해당 사용자의 할일 찾아오기
	private TbTodo findTodo(Long idxUser, Long idxTodo) {
		return todoRepository.findByIdxUserAndIdxTodoAndDeleteDateIsNull(idxUser, idxTodo)
								.orElseThrow(() -> CustomException.notFound("할일을 찾을 수 없습니다."));
	}
	
	// 목록 조회에서 사용할 카테고리 이름 추출 => 하나씩 추출하게 될 경우 양이 너무 많아질 수 있음. 그래서 현재 유저의 카테고리를 한번에 가져오기
	private String getCategoryNameFromMap(Long idxCat, Map<Long, String> categoryNameMap) {
		if (idxCat == null) {
			return "미분류";
		}
		
		// idxCat은 있는데, 조회 결과가 없음 => 삭제된 카테고리
		return categoryNameMap.getOrDefault(idxCat, "미분류");
	}
	
	// 단건 처리에서 사용할 카테고리 이름 조회
	private String getCategoryName(Long idxUser, Long idxCat) {
		if (idxCat == null) {
			return "미분류";
		}
		
		return categoryRepository
				.findByIdxCatAndIdxUserAndDeleteDateIsNull(idxCat, idxUser)
				.map(TbCategory::getName)
				.orElse("미분류");
	}
}
