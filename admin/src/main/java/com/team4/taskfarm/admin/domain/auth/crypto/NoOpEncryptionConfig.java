package com.team4.taskfarm.admin.domain.auth.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class NoOpEncryptionConfig {

    @Bean
    @ConditionalOnMissingBean(EncryptionService.class)
    public EncryptionService noOpEncryptionService() {
        log.warn("[MFA] ⚠️ NoOp 암호화 사용 중 — 시크릿이 평문 저장됩니다. " +
                 "로컬 외 환경이면 mfa.kms.enabled=true 로 KMS를 켜세요.");
        return new EncryptionService() {
            @Override public String encrypt(String plaintext)  { return plaintext; }
            @Override public String decrypt(String ciphertext) { return ciphertext; }
        };
    }
}