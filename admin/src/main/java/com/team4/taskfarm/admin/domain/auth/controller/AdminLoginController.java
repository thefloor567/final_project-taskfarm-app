package com.team4.taskfarm.admin.domain.auth.controller;
 
import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.admin.domain.auth.service.AdminLoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminLoginController extends AdminBaseController {
 
    private final AdminLoginService loginService;
 
    // POST /admin/api/login (context-path 자동)
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ok(loginService.login(request));
    }
}
 