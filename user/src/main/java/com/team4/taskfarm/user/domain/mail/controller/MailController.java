package com.team4.taskfarm.user.domain.mail.controller;

import com.team4.taskfarm.user.common.UserBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MailController extends UserBaseController {

    @GetMapping("/mails")
    public String mails() {
        return "mail/mail";
    }
}
