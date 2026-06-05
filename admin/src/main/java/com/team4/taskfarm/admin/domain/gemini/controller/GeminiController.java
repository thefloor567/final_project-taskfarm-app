package com.team4.taskfarm.admin.domain.gemini.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeminiController extends AdminBaseController {

    // 실제 URL: /admin/gemini
    @GetMapping("/gemini")
    public String usage() {
        // 🔜 if (!isAdmin()) return "redirect:/";
        return "gemini/usage";
    }
}