package com.team4.taskfarm.admin.domain.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    // context-path가 /admin 이므로 실제 URL은 /admin/  (로그인)
    @GetMapping("/")
    public String login() {
        return "auth/login";
    }
}