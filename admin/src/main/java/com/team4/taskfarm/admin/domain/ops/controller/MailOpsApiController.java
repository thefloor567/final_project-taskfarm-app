package com.team4.taskfarm.admin.domain.ops.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.ops.dto.MailBroadcastRequest;
import com.team4.taskfarm.admin.domain.ops.dto.MailBroadcastResponse;
import com.team4.taskfarm.admin.domain.ops.dto.MailHistoryResponse;
import com.team4.taskfarm.admin.domain.ops.service.MailOpsService;
import com.team4.taskfarm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ops/mails")
@RequiredArgsConstructor
public class MailOpsApiController extends AdminBaseController {

    private final MailOpsService mailOpsService;

    // POST /admin/api/ops/mails/broadcast
    @PostMapping("/broadcast")
    public ResponseEntity<ApiResponse<MailBroadcastResponse>> broadcast(
            @Valid @RequestBody MailBroadcastRequest request) {
        return ok(mailOpsService.broadcast(request));
    }

    // GET /admin/api/ops/mails/history?limit=20
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<MailHistoryResponse>>> history(
            @RequestParam(defaultValue = "20") int limit) {
        return ok(mailOpsService.getHistory(limit));
    }
}
