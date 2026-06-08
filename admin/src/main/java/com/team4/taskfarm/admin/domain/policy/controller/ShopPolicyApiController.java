package com.team4.taskfarm.admin.domain.policy.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.policy.dto.SeedPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.dto.SeedPolicyResponse;
import com.team4.taskfarm.admin.domain.policy.dto.ToolPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.dto.ToolPolicyResponse;
import com.team4.taskfarm.admin.domain.policy.service.ShopPolicyService;
import com.team4.taskfarm.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policies/shop")
@RequiredArgsConstructor
public class ShopPolicyApiController extends AdminBaseController {

    private final ShopPolicyService shopPolicyService;

    // GET /admin/api/policies/shop/seeds
    @GetMapping("/seeds")
    public ResponseEntity<ApiResponse<List<SeedPolicyResponse>>> getSeeds() {
        return ok(shopPolicyService.getSeeds());
    }

    // GET /admin/api/policies/shop/tools
    @GetMapping("/tools")
    public ResponseEntity<ApiResponse<List<ToolPolicyResponse>>> getTools() {
        return ok(shopPolicyService.getTools());
    }

    // PATCH /admin/api/policies/shop/seeds/{id}
    @PatchMapping("/seeds/{id}")
    public ResponseEntity<ApiResponse<SeedPolicyResponse>> updateSeed(
            @PathVariable Long id, @Valid @RequestBody SeedPolicyRequest request) {
        return ok(shopPolicyService.updateSeed(id, request));
    }

    // PATCH /admin/api/policies/shop/tools/{id}
    @PatchMapping("/tools/{id}")
    public ResponseEntity<ApiResponse<ToolPolicyResponse>> updateTool(
            @PathVariable Long id, @Valid @RequestBody ToolPolicyRequest request) {
        return ok(shopPolicyService.updateTool(id, request));
    }
}
