package com.team4.taskfarm.admin.domain.auth.service;

import com.team4.taskfarm.admin.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.admin.domain.auth.dto.LoginResponse;
import com.team4.taskfarm.admin.domain.auth.mfa.MfaService;
import com.team4.taskfarm.admin.domain.auth.repository.AdminUserRepository;
import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    private final AdminUserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final MfaService mfaService;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 유저 조회
        TbUser user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.notFound("계정을 찾을 수 없습니다."));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPass())) {
            throw CustomException.unauthorized("비밀번호가 일치하지 않습니다.");
        }

        // 3. ROLE_ADMIN 검증 — 일반 유저는 어드민 로그인 불가
        if (!"ROLE_ADMIN".equals(user.getRole().name())) {
            throw CustomException.forbidden("관리자 권한이 없습니다.");
        }

        // 4. MFA 미등록 → 강제 등록 유도 (토큰 발급 안 함)
        if (!user.requiresMfa()) {
            return LoginResponse.setupRequired(user.getEmail());
        }

        // 5. MFA 등록됨 → OTP 코드 확인
        String otp = request.getOtpCode();
        if (otp == null || otp.isBlank()) {
            // 아직 OTP 안 줌 → OTP 입력 단계로
            return LoginResponse.mfaRequired(user.getEmail());
        }

        // 6. OTP 검증 (저장된 암호화 시크릿 복호화하여 비교)
        if (!mfaService.verifyEncrypted(user.getMfaSecret(), otp)) {
            throw CustomException.unauthorized("인증 코드가 일치하지 않습니다.");
        }

        // 7. 모두 통과 → JWT 발급
        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name(),    // "ROLE_ADMIN"
                user.getIdxUser()
        );

        return LoginResponse.success(token, user.getEmail(), user.getNickname());
    }
}