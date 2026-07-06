package com.team4.taskfarm.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 동작 확인용. context-path=/admin 이므로 실제 호출은 GET /admin/version
 */
@RestController
public class VersionController {

    @GetMapping("/version")
    public String version() {
        return "taskfarm-admin version: v2026.0706.1";
    }
}
