package com.team4.taskfarm.user.domain.home.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeResponse {
	private String nickName;
	private int level;
	private int water;
	private int coin;
	private int xpNow;
	private int xpMax;
	private long totalTodo;
	private long doneTodo;
}
