package com.team4.taskfarm.admin.domain.auth.mfa.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MfaSetupRequest {
    @NotBlank private String email;
    @NotBlank private String password;
}