package com.team4.taskfarm.common.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean result;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.result = true;
        res.message = "success";
        res.data = data;
        return res;
    }

    public static <T> ApiResponse<T> success() {
        ApiResponse<T> res = new ApiResponse<>();
        res.result = true;
        res.message = "success";
        return res;
    }

    public static <T> ApiResponse<T> fail(String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.result = false;
        res.message = message;
        return res;
    }
}
