package com.team4.taskfarm.user.domain.ai.policy;

import com.team4.taskfarm.common.entity.todo.TbTodo.Priority;

public class ExpClampPolicy {
    private ExpClampPolicy() {}

    /** min~max로 제한 (값은 DB 정책에서 주입) */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}