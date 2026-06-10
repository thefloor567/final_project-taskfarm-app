package com.team4.taskfarm.user.domain.todo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.ai.TbAiLog;
import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.entity.category.TbCategory;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobRequest;
import com.team4.taskfarm.user.domain.ai.dto.AiRecommendJobResult;
import com.team4.taskfarm.user.domain.ai.repository.AiLogRepository;
import com.team4.taskfarm.user.domain.ai.service.AiRecommendQueueService;
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
	
	// Todo별 최신 AI 추천 경험치 조회용
	private final AiLogRepository aiLogRepository;
	
	// AI 추천 작업 Redis 큐 접수/조회용
	private final AiRecommendQueueService aiRecommendQueueService;
	
	private final RewardService rewardService;
	
	// 할일 가져오기
	@Transactional(readOnly = true)
	public List<TodoResponse> getTodoList(Long idxUser, Boolean isDone, Long idxCat, LocalDate dueDate){
		
		LocalDateTime start = (dueDate != null) ? dueDate.atStartOfDay() : null;
	    LocalDateTime end   = (dueDate != null) ? dueDate.plusDays(1).atStartOfDay() : null;

	    List<TbTodo> todo = todoRepository.search(idxUser, isDone, idxCat, start, end);

		// 현재 유저의 카테고리 목록을 한 번만 조회
	    Map<Long, String> categoryNameMap = categoryRepository.findByIdxUserAndDeleteDateIsNull(idxUser)
                .stream()
                .collect(Collectors.toMap(TbCategory::getIdxCat, TbCategory::getName));
	    
	    Map<Long, Integer> rewardMap = aiLogRepository.findByIdxUserOrderByCreateDateAsc(idxUser)
	    		.stream()
	    		.filter(log -> log.getIdxTodo() != null)
	    		.collect(Collectors.toMap(TbAiLog::getIdxTodo, TbAiLog::getRewardExp, (old, recent) -> recent));

	    return todo.stream()
                .map(t -> TodoResponse.from(
                        t,
                        getCategoryNameFromMap(t.getIdxCat(), categoryNameMap),
                        rewardMap.getOrDefault(t.getIdxTodo(), 0)))   // ← rewardExp 인자 추가
                .toList();
    }
	
	// 단건 조회
	@Transactional(readOnly = true)
	public TodoResponse getTodo(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);
	    String categoryName = getCategoryName(idxUser, todo.getIdxCat());
	    int rewardExp = getLatestRewardExp(idxUser, idxTodo);
	    return TodoResponse.from(todo, categoryName, rewardExp);
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
		return TodoResponse.from(savedTodo, categoryName, 0);
	}
	
	// findTodo에서 찾아오고 업데이트
	@Transactional
	public TodoResponse updateTodo(Long idxUser, Long idxTodo, TodoRequest request) {
		TbTodo todo = findTodo(idxUser, idxTodo);
		todo.update(request.getIdxCat(), request.getTitle(), request.getContent(), request.getPriority(), request.getDueDate());
		String categoryName = getCategoryName(idxUser, todo.getIdxCat());
		int rewardExp = getLatestRewardExp(idxUser, todo.getIdxTodo());
		return TodoResponse.from(todo, categoryName, rewardExp);
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

		boolean wasDone = todo.isDone();      // 완료 전 상태 기록
		todo.complete();

		// 최초 완료에만 보상 지급 (이미 완료였던 걸 다시 완료해도 재지급 X — 파밍 방지)
		if (!wasDone) {
			rewardService.grantTodoDone(idxUser, todo.getPriority(), todo.getIdxTodo());
		}
		String categoryName = getCategoryName(idxUser, todo.getIdxCat());
		int rewardExp = getLatestRewardExp(idxUser, todo.getIdxTodo());
		return TodoResponse.from(todo, categoryName, rewardExp);
	}
	
	// 할일 완료 해제
	// 생각 필요 => 할일 해제 시 기존에 들어갔던 경험치는 유저의 경험치에서 빼지 않고, 나중에 이 할일 다시 완료 시 경험치 지급 X
	@Transactional
	public TodoResponse incompleteTodo(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);
	    todo.incomplete();
	    String categoryName = getCategoryName(idxUser, todo.getIdxCat());
	    int rewardExp = getLatestRewardExp(idxUser, todo.getIdxTodo());
	    return TodoResponse.from(todo, categoryName, rewardExp);
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
	
	// AI 추천 기록이 있으면 RewardExp 반환
	private int getLatestRewardExp(Long idxUser, Long idxTodo) {
	    return aiLogRepository
	            .findTopByIdxUserAndIdxTodoOrderByCreateDateDesc(idxUser, idxTodo)
	            .map(TbAiLog::getRewardExp)
	            .orElse(0);
	}
	
	// AI 경험치 추천 비동기 접수
	@Transactional(readOnly = true)
	public AiRecommendJobResult requestRecommendExp(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);

	    String categoryName = getCategoryName(idxUser, todo.getIdxCat());

	    AiRecommendJobRequest request = new AiRecommendJobRequest(
	            null, // jobId는 QueueService에서 생성
	            idxUser,
	            idxTodo,
	            categoryName,
	            todo.getPriority(),
	            todo.getTitle()
	    );

	    return aiRecommendQueueService.enqueue(request);
	}
	
	// AI 경험치 추천 작업 결과 조회
	@Transactional(readOnly = true)
	public AiRecommendJobResult getRecommendExpJobResult(String jobId) {
	    return aiRecommendQueueService.getJobResult(jobId);
	}
}
