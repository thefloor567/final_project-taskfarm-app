package com.team4.taskfarm.admin.domain.ops.controller;

import com.team4.taskfarm.admin.common.AdminBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 운영 화면(랭킹 감사 / 우편 발송) 뷰 라우트.
 */
@Controller
public class OpsViewController extends AdminBaseController {

    // 실제 URL: /admin/rank
    @GetMapping("/rank")
    public String rankAudit() {
        return "rank/rank";
    }

    // 실제 URL: /admin/mails
    @GetMapping("/mails")
    public String mailSend() {
        return "mail/mail";
    }
}
