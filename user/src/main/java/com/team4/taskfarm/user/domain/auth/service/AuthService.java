package com.team4.taskfarm.user.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.config.TokenBlacklist;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.LoginResponse;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.common.entity.user.TbUser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

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

    
}