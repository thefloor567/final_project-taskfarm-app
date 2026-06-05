package com.team4.taskfarm.admin.domain.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller // API가 아니라 화면을 띄울 거니까 @Controller
public class AdminLoginViewController {

    // 1. 로그인 페이지를 보여주는 역할 (권한 체크 X)
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // templates/auth/login.html
    }
}