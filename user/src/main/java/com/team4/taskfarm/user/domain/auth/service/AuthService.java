package com.team4.taskfarm.user.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.LoginResponse;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.auth.dto.UserResponse;
import com.team4.taskfarm.user.domain.auth.repository.UserRepository;
import com.team4.taskfarm.user.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public void signup(SignupRequest req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw CustomException.badRequest("이미 사용중인 이메일입니다.");
        }

        User user = User.create(
            req.getEmail(),
            passwordEncoder.encode(req.getPassword()),
            req.getNickname()
        );

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> CustomException.unauthorized("존재하지 않는 이메일입니다."));

        if (user.getDeleteDate() != null || user.getStatus() == User.Status.SUSPENDED) {
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

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userIdx) {
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }

    @Transactional
    public void updateProfile(Long userIdx, UpdateProfileRequest req) {
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        if (req.getNickname() != null) {
            user.updateNickname(req.getNickname());
        }
    }

    @Transactional
    public void withdraw(Long userIdx) {
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        user.withdraw();
    }
}