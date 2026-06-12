package com.team4.taskfarm.user.domain.auth.service;

import java.security.SecureRandom;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.config.TokenBlacklist;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.LoginResponse;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String FRIEND_CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int FRIEND_CODE_LENGTH = 8;
    private static final int FRIEND_CODE_RETRY_COUNT = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklist tokenBlacklist;

    @Transactional
    public void signup(SignupRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw CustomException.badRequest("이미 사용중인 이메일입니다.");
        }

        TbUser user = TbUser.create(
            req.getEmail(),
            passwordEncoder.encode(req.getPassword()),
            req.getNickname()
        );

        user.assignFriendCode(generateUniqueFriendCode());

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        TbUser user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> CustomException.unauthorized("존재하지 않는 이메일입니다."));

        if (user.getDeleteDate() != null || user.getStatus() == TbUser.Status.SUSPENDED) {
            throw CustomException.unauthorized("탈퇴한 계정입니다.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPass())) {
            throw CustomException.unauthorized("비밀번호가 일치하지 않습니다.");
        }

        String token = jwtService.generateToken(
            user.getEmail(),
            user.getRole().name(),
            user.getIdxUser()
        );

        return LoginResponse.builder()
            .token(token)
            .build();
    }

    public void logout(String token) {
        if (token == null) return;
        tokenBlacklist.blacklist(token, jwtService.getExpiresAt(token));
    }

    private String generateUniqueFriendCode() {
        for (int i = 0; i < FRIEND_CODE_RETRY_COUNT; i++) {
            String code = generateFriendCode();

            if (userRepository.findByFriendCode(code).isEmpty()) {
                return code;
            }
        }

        throw CustomException.badRequest("친구코드 생성에 실패했습니다. 다시 시도해주세요.");
    }

    private String generateFriendCode() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < FRIEND_CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(FRIEND_CODE_CHARS.length());
            sb.append(FRIEND_CODE_CHARS.charAt(index));
        }

        return sb.toString();
    }
}