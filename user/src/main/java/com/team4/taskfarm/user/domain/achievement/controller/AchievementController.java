package com.team4.taskfarm.user.domain.achievement.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AchievementController extends UserBaseController {

    @GetMapping("/achievements")
    public String achievements() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "achievement/achievements";
    }
}