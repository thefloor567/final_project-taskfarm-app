package com.team4.taskfarm.user.domain.home.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 홈(대시보드) 뷰.
 * 닉네임/레벨 등 데이터는 /api/home, /api/auth/me 에서 JS로 가져옴 (뷰는 화면만 결정).
 */
@Controller
public class HomeController extends UserBaseController {

    @GetMapping("/home")
    public String home() {
        // 🔜 인증 붙이면 주석 해제
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "home";
    }
}
