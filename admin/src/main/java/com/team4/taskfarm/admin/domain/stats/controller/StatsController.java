package com.team4.taskfarm.admin.domain.stats.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController extends AdminBaseController {

    // 실제 URL: /admin/stats
    @GetMapping("/stats")
    public String stats() {
        // 🔜 if (!isAdmin()) return "redirect:/";
        return "stats/stats";
    }
}