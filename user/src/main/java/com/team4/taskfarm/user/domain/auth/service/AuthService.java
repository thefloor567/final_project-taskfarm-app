package com.team4.taskfarm.user.domain.auth.service;

import org.springframework.stereotype.Service;
// import org.springframework.security.crypto.password.PasswordEncoder; // 공통모듈 완성 후 활성화

import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.auth.dto.LoginRequest;
import com.team4.taskfarm.user.domain.auth.dto.SignupRequest;
import com.team4.taskfarm.user.domain.auth.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.auth.dto.UserResponse;
import com.team4.taskfarm.user.domain.auth.repository.UserRepository;
import com.team4.taskfarm.user.domain.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // 공통모듈 완성 후 활성화
    // private final JwtTokenProvider jwtTokenProvider; // 공통모듈 완성 후 활성화

    @Transactional
    public void signup(SignupRequest req) {
        User user = User.create(
            req.getEmail(),
            req.getPassword(), // TODO: 공통모듈 완성 후 → passwordEncoder.encode(req.getPassword())
            req.getNickname()
        );
        userRepository.save(user);
    }

    @Transactional
    public String login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> CustomException.unauthorized("존재하지 않는 이메일입니다."));

     // 탈퇴한 계정 로그인 차단
        if (user.getDeleteDate() != null || user.getStatus() == User.Status.SUSPENDED) {
            throw CustomException.unauthorized("탈퇴한 계정입니다.");
        }

        // TODO: 공통모듈 완성 후 아래 주석 활성화
        // if (!passwordEncoder.matches(req.getPassword(), user.getPass())) {
        //     throw CustomException.unauthorized("비밀번호가 일치하지 않습니다.");
        // }
        // return jwtTokenProvider.createToken(user.getIdxUser(), user.getRole().name());

        return "임시토큰"; // TODO: 공통모듈 완성 후 제거
    }

    @Transactional
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

        // TODO: 공통모듈 완성 후 아래 주석 활성화
        // if (req.getNewPassword() != null) {
        //     user.updatePass(passwordEncoder.encode(req.getNewPassword()));
        // }
    }

    @Transactional
    public void withdraw(Long userIdx) {
        User user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));
        user.withdraw();
    }
}