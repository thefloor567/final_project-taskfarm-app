package com.team4.taskfarm.user.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

//받아야 할 것: 닉네임 (또는 비밀번호 변경용)
@Getter @NoArgsConstructor
public class UpdateProfileRequest {
 private String nickname;
 private String newPassword;
}