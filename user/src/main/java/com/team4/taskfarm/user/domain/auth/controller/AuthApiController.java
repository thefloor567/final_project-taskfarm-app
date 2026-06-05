package com.team4.taskfarm.user.domain.auth.controller;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.auth.dto.UserResponse;
import com.team4.taskfarm.user.domain.auth.service.AuthService;

// TODO: 공통모듈 완성 후 아래 주석 풀기
// import com.team4.taskfarm.user.common.UserBaseController;
// → UserBaseController 상속하면 ok(), getCurrentUserIdx() 자동으로 사용 가능

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
// TODO: 공통모듈 완성 후 → public class AuthApiController extends UserBaseController 로 변경
public class AuthApiController {

    private final AuthService authService;

    // =========================================
    // 임시 유저 인덱스 (공통모듈 완성 전까지만 사용)
    // TODO: 공통모듈 완성 후 이 줄 삭제하고
    //       메서드 안에서 getCurrentUserIdx() 로 교체
    // =========================================
    private Long tempUserIdx = 1L;

    // 회원가입
    @PostMapping("/signup")
    public String signup(@Valid @RequestBody SignupRequest req) {
        authService.signup(req);
        // TODO: 공통모듈 완성 후 → return ok(); 로 교체
        return "회원가입 성공";
    }

    // 로그인
    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest req) {
        // TODO: 공통모듈 완성 후 → return ok(authService.login(req)); 로 교체
        return authService.login(req);
    }

    // 프로필 조회
    @GetMapping("/me")
    public UserResponse getProfile() {
        // TODO: 공통모듈 완성 후 → authService.getProfile(getCurrentUserIdx()) 로 교체
        return authService.getProfile(tempUserIdx);
    }

    // 프로필 수정
    @PutMapping("/me")
    public String updateProfile(@RequestBody UpdateProfileRequest req) {
        // TODO: 공통모듈 완성 후 → authService.updateProfile(getCurrentUserIdx(), req) 로 교체
        authService.updateProfile(tempUserIdx, req);
        // TODO: 공통모듈 완성 후 → return ok(); 로 교체
        return "수정 완료";
    }

    // 탈퇴
    @DeleteMapping("/me")
    public String withdraw() {
        // TODO: 공통모듈 완성 후 → authService.withdraw(getCurrentUserIdx()) 로 교체
        authService.withdraw(tempUserIdx);
        // TODO: 공통모듈 완성 후 → return ok(); 로 교체
        return "탈퇴 완료";
    }
}