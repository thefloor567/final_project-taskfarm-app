package com.team4.taskfarm.user.domain.user.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MypageController extends UserBaseController {

    @GetMapping("/mypage")
    public String mypage() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "user/mypage";
    }
}