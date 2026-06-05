package com.team4.taskfarm.user.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

//받아야 할 것: 이메일, 비밀번호
@Getter @NoArgsConstructor
public class LoginRequest {
 @NotBlank private String email;
 @NotBlank private String password;
}