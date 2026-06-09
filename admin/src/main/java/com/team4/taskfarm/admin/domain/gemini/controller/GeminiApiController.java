package com.team4.taskfarm.admin.domain.gemini.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.gemini.dto.GeminiUsageResponse;
import com.team4.taskfarm.admin.domain.gemini.service.GeminiMonitorService;
import com.team4.taskfarm.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gemini")
@RequiredArgsConstructor
public class GeminiApiController extends AdminBaseController {

    private final GeminiMonitorService geminiMonitorService;

    // GET /admin/api/gemini/usage
    @GetMapping("/usage")
    public ResponseEntity<ApiResponse<GeminiUsageResponse>> getUsage() {
        return ok(geminiMonitorService.getUsage());
    }
}
