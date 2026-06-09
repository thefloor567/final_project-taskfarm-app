package com.team4.taskfarm.user.domain.ai.policy;

import com.team4.taskfarm.common.entity.todo.TbTodo.Priority;

public class ExpClampPolicy {

    private ExpClampPolicy() {
    }

    public static int clamp(Priority priority, int exp) {
        return switch (priority) {
            case A -> clamp(exp, 10, 60);
            case B -> clamp(exp, 5, 30);
            case C -> clamp(exp, 1, 15);
        };
    }

    public static int baseExp(Priority priority) {
        return switch (priority) {
            case A -> 30;
            case B -> 15;
            case C -> 8;
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

}