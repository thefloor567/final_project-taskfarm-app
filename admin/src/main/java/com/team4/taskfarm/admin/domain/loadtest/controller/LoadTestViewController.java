package com.team4.taskfarm.admin.domain.loadtest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoadTestViewController {

    @GetMapping("/loadtest")
    public String loadtest(Model model) {
        model.addAttribute("active", "loadtest");
        return "loadtest/loadtest";
    }
}
