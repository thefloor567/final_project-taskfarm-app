package com.team4.taskfarm.user.domain.mail.controller;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.mail.dto.MailBoxResponse;
import com.team4.taskfarm.user.domain.mail.dto.MailClaimResponse;
import com.team4.taskfarm.user.domain.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mails")
@RequiredArgsConstructor
public class MailApiController extends UserBaseController {

    private final MailService mailService;

    @GetMapping
    public ResponseEntity<ApiResponse<MailBoxResponse>> getMails() {
        return ok(mailService.getMailBox(getCurrentUserIdx()));
    }

    @PostMapping("/{mailId}/claim")
    public ResponseEntity<ApiResponse<MailClaimResponse>> claimMail(@PathVariable Long mailId) {
        return ok(mailService.claimMail(getCurrentUserIdx(), mailId));
    }

    @PostMapping("/claim-all")
    public ResponseEntity<ApiResponse<MailClaimResponse>> claimAll() {
        return ok(mailService.claimAll(getCurrentUserIdx()));
    }
}
