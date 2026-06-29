package com.team4.taskfarm.admin.domain.auth.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
@ConditionalOnProperty(name = "mfa.kms.enabled", havingValue = "true")
public class KmsConfig {

    @Bean
    public KmsClient kmsClient(@Value("${mfa.kms.region:ap-northeast-2}") String region) {
        return KmsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}