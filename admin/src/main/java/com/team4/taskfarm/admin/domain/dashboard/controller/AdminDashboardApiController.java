package com.team4.taskfarm.admin.domain.dashboard.controller;
 
import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
 
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminDashboardApiController extends AdminBaseController {
 
    private final DashboardService dashboardService;
 
    // GET /admin/api/home (context-path 자동)
    @GetMapping("/home")
    public ResponseEntity<?> getAdminHome() {
        return ok(dashboardService.getAdminHome());
    }
}
