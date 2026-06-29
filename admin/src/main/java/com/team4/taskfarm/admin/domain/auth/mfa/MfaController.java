package com.team4.taskfarm.admin.domain.auth.mfa;

import com.team4.taskfarm.admin.common.AdminBaseController;
import com.team4.taskfarm.admin.domain.auth.dto.LoginResponse;
import com.team4.taskfarm.admin.domain.auth.mfa.dto.MfaConfirmSetupRequest;
import com.team4.taskfarm.admin.domain.auth.mfa.dto.MfaSetupRequest;
import com.team4.taskfarm.admin.domain.auth.mfa.dto.MfaSetupResponse;
import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mfa")
@RequiredArgsConstructor
public class MfaController extends AdminBaseController {

    private final MfaService mfaService;
    private final JwtService jwtService;
    
    //등록 1단계: 이메일+비번 본인확인 → 시크릿+QR
    @PostMapping("/setup")
    public ResponseEntity<?> setup(@Valid @RequestBody MfaSetupRequest req) {
        mfaService.authenticateAdmin(req.getEmail(), req.getPassword());
        MfaSetupResponse res = mfaService.startSetup(req.getEmail());
        return ok(res);
    }

    // 등록 2단계: 본인확인 + OTP 검증 → 저장 + 토큰 발급
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@Valid @RequestBody MfaConfirmSetupRequest req) {
        TbUser user = mfaService.authenticateAdmin(req.getEmail(), req.getPassword());
        mfaService.confirmSetup(req.getEmail(), req.getSecret(), req.getCode());
        String token = jwtService.generateToken(
                user.getEmail(), user.getRole().name(), user.getIdxUser());
        return ok(LoginResponse.success(token, user.getEmail(), user.getNickname()));
    }

    // 재등록 1단계: (로그인된 본인) 기존 MFA 해제 후 새 시크릿+QR
    @PostMapping("/reset/setup")
    public ResponseEntity<?> resetSetup() {
        String email = requireLogin();
        mfaService.disableMfa(email);
        MfaSetupResponse res = mfaService.startSetup(email);
        return ok(res);
    }

    // 재등록 2단계: OTP 검증 후 저장
    @PostMapping("/reset/confirm")
    public ResponseEntity<?> resetConfirm(@Valid @RequestBody MfaConfirmSetupRequest req) {
        String email = requireLogin();
        if (!email.equals(req.getEmail())) {
            throw CustomException.forbidden("본인 계정만 변경할 수 있습니다.");
        }
        mfaService.confirmSetup(email, req.getSecret(), req.getCode());
        return ok();
    }

    // 해제: (로그인된 본인) MFA 끄기
    @PostMapping("/disable")
    public ResponseEntity<?> disable() {
        String email = requireLogin();
        mfaService.disableMfa(email);
        return ok();
    }

    // 로그인된 본인 이메일 반환. 없으면 401
    private String requireLogin() {
        String email = getCurrentUserId();
        if (email == null) {
            throw CustomException.unauthorized("로그인이 필요합니다.");
        }
        return email;
    }
}