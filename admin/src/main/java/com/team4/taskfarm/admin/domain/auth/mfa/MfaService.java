package com.team4.taskfarm.admin.domain.auth.mfa;

import com.team4.taskfarm.admin.domain.auth.crypto.EncryptionService;
import com.team4.taskfarm.admin.domain.auth.mfa.dto.MfaSetupResponse;
import com.team4.taskfarm.admin.domain.auth.repository.AdminUserRepository;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private static final String ISSUER = "TaskFarm Admin";

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    private final EncryptionService encryptionService;
    private final AdminUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TbUser authenticateAdmin(String email, String rawPassword) {
        TbUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("계정을 찾을 수 없습니다."));
        if (!passwordEncoder.matches(rawPassword, user.getPass())) {
            throw CustomException.unauthorized("비밀번호가 일치하지 않습니다.");
        }
        if (!"ROLE_ADMIN".equals(user.getRole().name())) {
            throw CustomException.forbidden("관리자 권한이 없습니다.");
        }
        return user;
    }

    @Transactional
    public void disableMfa(String email) {
        TbUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("계정을 찾을 수 없습니다."));
        user.disableMfa();
        log.info("[MFA] 해제 email={}", email);
    }

    public MfaSetupResponse startSetup(String email) {
        TbUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("계정을 찾을 수 없습니다."));

        if (user.requiresMfa()) {
            throw CustomException.badRequest("이미 MFA가 등록되어 있습니다.");
        }

        String secret = secretGenerator.generate();

        QrData qrData = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(ISSUER)
                .digits(6)
                .period(30)
                .build();

        String qrDataUri;
        try {
            byte[] imageData = qrGenerator.generate(qrData);
            qrDataUri = getDataUriForImage(imageData, qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            log.error("[MFA] QR 생성 실패 email={}", email, e);
            throw CustomException.badRequest("QR 생성에 실패했습니다.");
        }

        return MfaSetupResponse.builder()
                .qrDataUri(qrDataUri)
                .secret(secret)
                .manualKey(secret)
                .build();
    }

    @Transactional
    public void confirmSetup(String email, String secret, String code) {
        TbUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> CustomException.notFound("계정을 찾을 수 없습니다."));

        if (!verifyCode(secret, code)) {
            throw CustomException.unauthorized("인증 코드가 일치하지 않습니다.");
        }
        String encrypted = encryptionService.encrypt(secret);
        user.enableMfa(encrypted);
        log.info("[MFA] 등록 완료 email={}", email);
    }

    public boolean verifyCode(String rawSecret, String code) {
        return codeVerifier.isValidCode(rawSecret, code);
    }

    public boolean verifyEncrypted(String encryptedSecret, String code) {
        String rawSecret = encryptionService.decrypt(encryptedSecret);
        return verifyCode(rawSecret, code);
    }
}