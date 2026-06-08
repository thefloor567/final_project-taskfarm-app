package com.team4.taskfarm.user.domain.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.auth.dto.UserResponse;
import com.team4.taskfarm.user.domain.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController extends UserBaseController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(req);
        return ok();
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest req) {
        return ok(authService.login(req));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // return ok(authService.getProfile(getCurrentUserIdx()));

        // 테스트용: 실제 DB에 존재하는 Idx_User 값으로 바꿔서 테스트
        return ok(authService.getProfile(1L));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateProfileRequest req) {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // authService.updateProfile(getCurrentUserIdx(), req);

        // 테스트용: 실제 DB에 존재하는 Idx_User 값으로 바꿔서 테스트
        authService.updateProfile(1L, req);
        return ok();
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw() {
        // TODO 최종버전: JWT/Auth 공통모듈 연동 후 아래 코드로 교체
        // authService.withdraw(getCurrentUserIdx());

        // 테스트용: 실제 DB에 존재하는 Idx_User 값으로 바꿔서 테스트
        authService.withdraw(1L);
        return ok();
    }
}