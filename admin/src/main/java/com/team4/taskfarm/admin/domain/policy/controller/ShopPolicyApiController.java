package com.team4.taskfarm.admin.domain.policy.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.policy.dto.ShopPolicyRequest;
import com.team4.taskfarm.admin.domain.policy.service.ShopPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies/shop")
@RequiredArgsConstructor
public class ShopPolicyApiController extends AdminBaseController {

    private final ShopPolicyService shopPolicyService;

    // GET /admin/api/policies/shop/seeds
    @GetMapping("/seeds")
    public ResponseEntity<?> getSeeds() {
        return ok(shopPolicyService.getSeeds());
    }

    // GET /admin/api/policies/shop/tools
    @GetMapping("/tools")
    public ResponseEntity<?> getTools() {
        return ok(shopPolicyService.getTools());
    }

    // PATCH /admin/api/policies/shop/seeds/{id}
    @PatchMapping("/seeds/{id}")
    public ResponseEntity<?> updateSeed(@PathVariable Long id, @Valid @RequestBody ShopPolicyRequest request) {
        return ok(shopPolicyService.updateSeed(id, request));
    }

    // PATCH /admin/api/policies/shop/tools/{id}
    @PatchMapping("/tools/{id}")
    public ResponseEntity<?> updateTool(@PathVariable Long id, @Valid @RequestBody ShopPolicyRequest request) {
        return ok(shopPolicyService.updateTool(id, request));
    }
}
