package com.team4.taskfarm.admin.domain.loadtest.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.loadtest.dto.LoadTestRequest;
import com.team4.taskfarm.admin.domain.loadtest.dto.RunIdResponse;
import com.team4.taskfarm.admin.domain.loadtest.dto.RunStatusResponse;
import com.team4.taskfarm.admin.domain.loadtest.service.LoadTestService;
import com.team4.taskfarm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loadtest")
@RequiredArgsConstructor
public class LoadTestController extends AdminBaseController {

    private final LoadTestService loadTestService;

    // POST /admin/api/loadtest/start
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<RunIdResponse>> start(@Valid @RequestBody LoadTestRequest request) {
        String runId = loadTestService.start(request);
        return ok(RunIdResponse.builder()
                .runId(runId)
                .build());
    }

    // GET /admin/api/loadtest/{runId}/status
    @GetMapping("/{runId}/status")
    public ResponseEntity<ApiResponse<RunStatusResponse>> status(@PathVariable String runId) {
        return ok(loadTestService.getStatus(runId));
    }

    // POST /admin/api/loadtest/{runId}/stop
    @PostMapping("/{runId}/stop")
    public ResponseEntity<ApiResponse<Void>> stop(@PathVariable String runId) {
        loadTestService.stop(runId);
        return ok();
    }
}
