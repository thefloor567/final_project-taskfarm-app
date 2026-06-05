package com.team4.taskfarm.user.domain.todo.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TodoController extends UserBaseController {

    @GetMapping("/todo/list")
    public String todoList() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "todo/list";
    }

    @GetMapping("/todo/add")
    public String todoAdd() {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        return "todo/add";
    }

    @GetMapping("/todo/detail/{todoId}")
    public String todoDetail(@PathVariable Long todoId, Model model) {
        // if (!isLoggedIn()) return "redirect:/auth/login";
        model.addAttribute("todoId", todoId);
        return "todo/detail";
    }
}