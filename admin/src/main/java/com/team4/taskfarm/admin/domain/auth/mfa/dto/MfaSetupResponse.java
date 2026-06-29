package com.team4.taskfarm.admin.domain.auth.mfa.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MfaSetupResponse {

    private String qrDataUri;

    private String secret;

    private String manualKey;
}