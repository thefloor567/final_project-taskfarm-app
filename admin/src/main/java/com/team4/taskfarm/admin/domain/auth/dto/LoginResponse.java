package com.team4.taskfarm.admin.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    public enum Status { SUCCESS, MFA_REQUIRED, SETUP_REQUIRED }

    private Status status;
    private String token;
    private String email;
    private String nickname;

    public static LoginResponse success(String token, String email, String nickname) {
        return LoginResponse.builder()
                .status(Status.SUCCESS)
                .token(token).email(email).nickname(nickname)
                .build();
    }

    public static LoginResponse mfaRequired(String email) {
        return LoginResponse.builder()
                .status(Status.MFA_REQUIRED).email(email)
                .build();
    }

    public static LoginResponse setupRequired(String email) {
        return LoginResponse.builder()
                .status(Status.SETUP_REQUIRED).email(email)
                .build();
    }
}