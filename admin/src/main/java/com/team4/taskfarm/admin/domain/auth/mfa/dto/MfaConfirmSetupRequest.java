package com.team4.taskfarm.admin.domain.auth.mfa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MfaConfirmSetupRequest {
    @NotBlank private String email;
    @NotBlank private String password;
    @NotBlank private String secret;
    @NotBlank @Pattern(regexp = "\\d{6}", message = "6자리 숫자를 입력하세요.")
    private String code;
}