package com.team4.taskfarm.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 동작 확인용 컨트롤러.
 * 앱 뜨면 GET /version 으로 "살아있는지" 바로 확인.
 */
@RestController
public class VersionController {

    @GetMapping("/version")
    public String version() {
        return "taskfarm-user version: v0.0.1";
    }
}
