package com.team4.taskfarm.user.domain.auth.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 인증 관련 뷰 페이지 (로그인/회원가입).
 * API는 같은 도메인의 AuthApiController(@RestController, /api/auth/**)가 담당.
 */
@Controller
public class AuthController extends UserBaseController {

    // 로그인 (시작 페이지)
    @GetMapping("/")
    public String login() {
        // 🔜 인증 붙이면 주석 해제: 이미 로그인 상태면 홈으로
        // if (isLoggedIn()) return "redirect:/home";
        return "auth/login";
    }

    // 회원가입
    @GetMapping("/signup")
    public String signup() {
        // if (isLoggedIn()) return "redirect:/home";
        return "auth/signup";
    }
}
