package com.team4.taskfarm.user.domain.auth.controller;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController extends UserBaseController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public Object signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(req);
        return ok();
    }

    // 로그인
    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest req) {
        return ok(authService.login(req));
    }

    // 프로필 조회
    @GetMapping("/me")
    public Object getProfile() {
        return ok(authService.getProfile(getCurrentUserIdx()));
    }

    // 프로필 수정
    @PutMapping("/me")
    public Object updateProfile(@RequestBody UpdateProfileRequest req) {
        authService.updateProfile(getCurrentUserIdx(), req);
        return ok();
    }

    // 탈퇴
    @DeleteMapping("/me")
    public Object withdraw() {
        authService.withdraw(getCurrentUserIdx());
        return ok();
    }
}