package com.team4.taskfarm.common.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PageResponse<T> {

    private boolean result;
    private String message;
    private List<T> data;
    private int pagePos;
    private int pageCnt;
    private long totalCnt;
    private int rowCnt;

    public static <T> PageResponse<T> success(
            List<T> data, int pagePos, int pageCnt, long totalCnt, int rowCnt) {
        return PageResponse.<T>builder()
                .result(true)
                .message("success")
                .data(data)
                .pagePos(pagePos)
                .pageCnt(pageCnt)
                .totalCnt(totalCnt)
                .rowCnt(rowCnt)
                .build();
    }

    public static <T> PageResponse<T> fail(String message) {
        return PageResponse.<T>builder()
                .result(false)
                .message(message)
                .build();
    }
}
