package com.team4.taskfarm.admin.domain.policy.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.policy.dto.EventConfigRequest;
import com.team4.taskfarm.admin.domain.policy.dto.EventConfigResponse;
import com.team4.taskfarm.admin.domain.policy.service.EventPolicyService;
import com.team4.taskfarm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies/events")
@RequiredArgsConstructor
public class EventPolicyApiController extends AdminBaseController {

    private final EventPolicyService eventPolicyService;

    // GET /admin/api/policies/events
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventConfigResponse>>> getAll() {
        return ok(eventPolicyService.getAll());
    }

    // PATCH /admin/api/policies/events/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<EventConfigResponse>> update(
            @PathVariable Long id, @Valid @RequestBody EventConfigRequest request) {
        return ok(eventPolicyService.update(id, request));
    }
}
