package com.team4.taskfarm.user.domain.friend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.team4.taskfarm.user.common.UserBaseController;

@Controller
public class SocialController extends UserBaseController {

    @GetMapping("/social")
    public String social() {
        return "social/social";
    }
}