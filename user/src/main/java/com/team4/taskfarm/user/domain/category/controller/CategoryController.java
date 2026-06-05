package com.team4.taskfarm.user.domain.category.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CategoryController extends UserBaseController {

    @GetMapping("/category")
    public String category() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "category/category";
    }
}