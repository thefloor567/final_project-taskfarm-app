package com.team4.taskfarm.admin.domain.policy.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController extends AdminBaseController {

    // 실제 URL: /admin/policies/exp
    @GetMapping("/policies/exp")
    public String expPolicy() {
        // 🔜 if (!isAdmin()) return "redirect:/";
        return "policies/exp";
    }

    @GetMapping("/policies/shop")
    public String shopPolicy() {
        // 🔜 if (!isAdmin()) return "redirect:/";
        return "policies/shop";
    }

    @GetMapping("/policies/events")
    public String eventPolicy() {
        // 🔜 if (!isAdmin()) return "redirect:/";
        return "policies/events";
    }
}