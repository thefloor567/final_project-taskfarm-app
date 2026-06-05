package com.team4.taskfarm.admin.domain.user.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class UserController extends AdminBaseController {

    // 실제 URL: /admin/users
    @GetMapping("/users")
    public String userList() {
        // 🔜 if (!isAdmin()) return "redirect:/";
        return "users/list";
    }

    // 실제 URL: /admin/users/{userId}
    @GetMapping("/users/{userId}")
    public String userDetail(@PathVariable Long userId, Model model) {
        // 🔜 if (!isAdmin()) return "redirect:/";
        model.addAttribute("userId", userId);
        return "users/detail";
    }
}