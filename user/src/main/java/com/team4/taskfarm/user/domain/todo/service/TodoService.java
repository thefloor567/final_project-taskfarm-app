package com.team4.taskfarm.user.domain.todo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.ai.TbAiLog;
import com.team4.taskfarm.common.entity.exp.TbExpLedger;
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
import com.team4.taskfarm.user.domain.todo.repository.TbExpLedgerRepository;
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

	// 실제 지급된 경험치(원장) 조회용 — "완료된 할일"의 정직한 표시값
	private final TbExpLedgerRepository expLedgerRepository;

	// AI 추천 작업 Redis 큐 접수/조회용
	private final AiRecommendQueueService aiRecommendQueueService;

	private final RewardService rewardService;

	/*
	 * [경험치 표시 원칙]
	 * rewardExp(화면의 'N XP')는 상태에 따라 의미가 다르다:
	 *   - 미완료: "완료하면 받을 예상치" = 최신 AI 추천값 (없으면 0)
	 *   - 완료:   "실제로 받은 값"     = 원장(TODO_DONE)에 기록된 지급액
	 * 완료는 1회만 지급되고 이후 바뀌지 않으므로(파밍 방지), 완료 후에는
	 * 최신 AI값이 아니라 '실제 지급액'을 보여줘야 화면이 거짓말하지 않는다.
	 */

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

	    // 미완료 표시용: Todo별 최신 AI 추천값
	    Map<Long, Integer> aiRewardMap = aiLogRepository.findByIdxUserOrderByCreateDateAsc(idxUser)
	    		.stream()
	    		.filter(log -> log.getIdxTodo() != null)
	    		.collect(Collectors.toMap(TbAiLog::getIdxTodo, TbAiLog::getRewardExp, (old, recent) -> recent));

	    // 완료 표시용: Todo별 실제 지급액(원장 EARN/TODO_DONE)
	    Map<Long, Integer> grantedMap = expLedgerRepository
	    		.findByIdxUserAndTypeAndReason(idxUser, TbExpLedger.LedgerType.EARN, "TODO_DONE")
	    		.stream()
	    		.filter(l -> l.getRefIdx() != null)
	    		.collect(Collectors.toMap(TbExpLedger::getRefIdx, TbExpLedger::getAmount, (old, recent) -> recent));

	    return todo.stream()
                .map(t -> TodoResponse.from(
                        t,
                        getCategoryNameFromMap(t.getIdxCat(), categoryNameMap),
                        // 완료면 실제 지급액, 미완료면 AI 예상치
                        t.isDone()
                            ? grantedMap.getOrDefault(t.getIdxTodo(), 0)
                            : aiRewardMap.getOrDefault(t.getIdxTodo(), 0)))
                .toList();
    }

	// 단건 조회
	@Transactional(readOnly = true)
	public TodoResponse getTodo(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);
	    String categoryName = getCategoryName(idxUser, todo.getIdxCat());
	    int rewardExp = resolveRewardExp(idxUser, todo);
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
		int rewardExp = resolveRewardExp(idxUser, todo);
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

		// 최초 완료에만 보상 지급 (이미 완료였던 걸 다시 완료해도 재지급 X — 파밍 방지).
		// grantTodoDone은 '실제 지급액'을 반환한다(이미 받았으면 그때 값).
		int rewardExp;
		if (!wasDone) {
			rewardExp = rewardService.grantTodoDone(idxUser, todo.getPriority(), todo.getIdxTodo());
		} else {
			// 이미 완료 상태였다면 원장의 실제 지급액을 그대로 표시
			rewardExp = rewardService.getGrantedExp(idxUser, todo.getIdxTodo());
		}

		String categoryName = getCategoryName(idxUser, todo.getIdxCat());
		return TodoResponse.from(todo, categoryName, rewardExp);
	}

	// 할일 완료 해제
	// 완료 해제 시 기존 지급 경험치는 회수하지 않고(유저 경험치 유지),
	// 원장 기록도 남겨둔다 → 재완료해도 재지급되지 않음(파밍 방지).
	@Transactional
	public TodoResponse incompleteTodo(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);
	    todo.incomplete();
	    String categoryName = getCategoryName(idxUser, todo.getIdxCat());
	    int rewardExp = resolveRewardExp(idxUser, todo);
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

	/**
	 * 화면 표시용 rewardExp 결정 (단건용).
	 *  - 완료된 할일: 원장에 기록된 '실제 지급액'
	 *  - 미완료 할일: 최신 AI 추천 '예상치'(없으면 0)
	 */
	private int resolveRewardExp(Long idxUser, TbTodo todo) {
		if (todo.isDone()) {
			return rewardService.getGrantedExp(idxUser, todo.getIdxTodo());
		}
		return getLatestAiRewardExp(idxUser, todo.getIdxTodo());
	}

	// AI 추천 기록이 있으면 RewardExp 반환 (미완료 표시용)
	private int getLatestAiRewardExp(Long idxUser, Long idxTodo) {
	    return aiLogRepository
	            .findTopByIdxUserAndIdxTodoOrderByCreateDateDesc(idxUser, idxTodo)
	            .map(TbAiLog::getRewardExp)
	            .orElse(0);
	}

	// AI 경험치 추천 비동기 접수
	@Transactional(readOnly = true)
	public AiRecommendJobResult requestRecommendExp(Long idxUser, Long idxTodo) {
	    TbTodo todo = findTodo(idxUser, idxTodo);

	    // 이미 완료된 할일은 경험치가 확정되었으므로 AI 재책정 의미 없음 → 막는다.
	    if (todo.isDone()) {
	        throw CustomException.badRequest("이미 완료된 할일은 경험치가 확정되어 다시 책정할 수 없습니다.");
	    }

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