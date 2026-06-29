package com.team4.taskfarm.admin.domain.auth.crypto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.EncryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@ConditionalOnProperty(name = "mfa.kms.enabled", havingValue = "true")
public class KmsEncryptionService implements EncryptionService {

    private final KmsClient kmsClient;
    private final String keyId;

    public KmsEncryptionService(KmsClient kmsClient,
                                @Value("${mfa.kms.key-id}") String keyId) {
        this.kmsClient = kmsClient;
        this.keyId = keyId;
        log.info("[MFA] KMS 암호화 활성화 (keyId={})", keyId);
    }

    @Override
    public String encrypt(String plaintext) {
        EncryptRequest request = EncryptRequest.builder()
                .keyId(keyId)
                .plaintext(SdkBytes.fromString(plaintext, StandardCharsets.UTF_8))
                .build();
        EncryptResponse response = kmsClient.encrypt(request);
        return Base64.getEncoder().encodeToString(response.ciphertextBlob().asByteArray());
    }

    @Override
    public String decrypt(String ciphertext) {
        byte[] decoded = Base64.getDecoder().decode(ciphertext);
        DecryptRequest request = DecryptRequest.builder()
                .keyId(keyId)
                .ciphertextBlob(SdkBytes.fromByteArray(decoded))
                .build();
        DecryptResponse response = kmsClient.decrypt(request);
        return response.plaintext().asUtf8String();
    }
}