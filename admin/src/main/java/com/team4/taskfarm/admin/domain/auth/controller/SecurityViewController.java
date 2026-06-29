package com.team4.taskfarm.admin.domain.auth.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SecurityViewController extends AdminBaseController {

    @GetMapping("/security")
    public String security() {
        return "security";
    }
}