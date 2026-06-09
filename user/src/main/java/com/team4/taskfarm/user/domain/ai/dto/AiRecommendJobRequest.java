package com.team4.taskfarm.user.domain.ai.dto;

import com.team4.taskfarm.common.entity.todo.TbTodo.Priority;


// Redis 큐에 들어갈 AI 추천 작업 정보.
public record AiRecommendJobRequest(
        String jobId,
        Long idxUser,
        Long idxTodo,
        String categoryName,
        Priority priority,
        String title
) {
}