package com.team4.taskfarm.user.domain.stats.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StatsController extends UserBaseController {

    @GetMapping("/stats")
    public String stats() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "stats/stats";
    }
}