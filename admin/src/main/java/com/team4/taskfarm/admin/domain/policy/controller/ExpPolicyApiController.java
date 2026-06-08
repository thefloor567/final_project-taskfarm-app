package com.team4.taskfarm.admin.domain.policy.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.policy.dto.ExpPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.service.ExpPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class ExpPolicyApiController extends AdminBaseController {

    private final ExpPolicyService expPolicyService;

    // GET /admin/api/policies/exp
    @GetMapping("/exp")
    public ResponseEntity<?> getAll() {
        return ok(expPolicyService.getAll());
    }

    // PATCH /admin/api/policies/exp/{id}
    @PatchMapping("/exp/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ExpPolicyRequest request) {
        return ok(expPolicyService.update(id, request));
    }
}
