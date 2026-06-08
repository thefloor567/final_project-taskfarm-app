package com.team4.taskfarm.admin.domain.auth.dto;
 
import lombok.Builder;
import lombok.Getter;
 
@Getter
@Builder
public class LoginResponse {
    private String token;
    private String email;
    private String nickname;
}