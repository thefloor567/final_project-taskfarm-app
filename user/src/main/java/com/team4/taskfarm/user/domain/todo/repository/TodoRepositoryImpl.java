package com.team4.taskfarm.user.domain.todo.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team4.taskfarm.common.entity.todo.TbTodo;
import com.team4.taskfarm.common.entity.todo.QTbTodo;

import java.time.LocalDateTime;
import java.util.List;


public class TodoRepositoryImpl implements TodoRepositoryCustom {
	
	private final JPAQueryFactory queryFactory = null;
	private static final QTbTodo todo = QTbTodo.tbTodo;
	
	public List<TbTodo> search(Long idxUser, Boolean isDone, Long idxCat, LocalDateTime start, LocalDateTime end){
		return queryFactory
				.selectFrom(todo)
				.where(
						todo.idxUser.eq(idxUser),
						todo.deleteDate.isNull(),
						isDoneEq(isDone),
						idxCatEq(idxCat),
						dueDateGoe(start),
						dueDateLt(end)
				)
				.orderBy(todo.createDate.desc())
				.fetch();
	}
	
	private BooleanExpression isDoneEq(Boolean v) { return v == null ? null : todo.isDone.eq(v); }
	private BooleanExpression idxCatEq(Long v) { return v == null ? null : todo.idxCat.eq(v); }
	private BooleanExpression dueDateGoe(LocalDateTime v) { return v == null ? null : todo.dueDate.goe(v); }
	private BooleanExpression dueDateLt(LocalDateTime v) { return v == null ? null : todo.dueDate.lt(v); }
}
