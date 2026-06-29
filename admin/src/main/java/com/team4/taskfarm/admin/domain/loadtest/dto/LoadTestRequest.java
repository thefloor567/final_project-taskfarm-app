package com.team4.taskfarm.admin.domain.loadtest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoadTestRequest {

    @NotBlank(message = "scenario는 필수입니다.")
    private String scenario;

    @Min(value = 1, message = "concurrency는 1 이상이어야 합니다.")
    private int concurrency;

    @Min(value = 1, message = "totalRequests는 1 이상이어야 합니다.")
    private int totalRequests;
}
