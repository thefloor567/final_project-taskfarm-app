package com.team4.taskfarm.admin.domain.dashboard.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController extends AdminBaseController {

    // 실제 URL: /admin/dashboard (context-path 자동)
    @GetMapping("/dashboard")
    public String dashboard() {
        // 🔜 인증 붙이면: if (!isAdmin()) return "redirect:/";
        return "dashboard";
    }
}