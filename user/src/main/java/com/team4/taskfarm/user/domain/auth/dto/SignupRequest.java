package com.team4.taskfarm.user.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

//받아야 할 것: 이메일, 비밀번호, 닉네임
@Getter @NoArgsConstructor
public class SignupRequest {
 @NotBlank private String email;
 @NotBlank private String password;
 @NotBlank private String nickname;
}