package com.team4.taskfarm.admin.domain.user.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.user.dto.UserDetailResponse;
import com.team4.taskfarm.admin.domain.user.dto.UserListResponse;
import com.team4.taskfarm.admin.domain.user.service.AdminUserService;
import com.team4.taskfarm.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AdminUserApiController extends AdminBaseController {

    private final AdminUserService adminUserService;

    // GET /admin/api/users
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserListResponse>>> getUserList() {
        return ok(adminUserService.getUserList());
    }

    // GET /admin/api/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long userId) {
        return ok(adminUserService.getUserDetail(userId));
    }
}
