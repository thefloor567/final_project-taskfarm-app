package com.team4.taskfarm.admin.domain.loadtest.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RunStatusResponse {

    private String status;
    private int total;
    private int processed;
    private int success;
    private int failed;
    private int successRate;
    private long avgMs;
    private long p95Ms;
    private int queueLength;
    private int podCount;
    private int maxPods;
}
