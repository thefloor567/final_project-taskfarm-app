package com.team4.taskfarm.user.domain.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.config.TokenBlacklist;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.user.dto.ChangePasswordRequest;
import com.team4.taskfarm.user.domain.user.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.user.dto.UserResponse;
import com.team4.taskfarm.user.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklist tokenBlacklist;
    private final JwtService jwtService;
    
    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userIdx) {
        TbUser user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        return UserResponse.from(user);
    }
    
    @Transactional
    public void updateProfile(Long userIdx, UpdateProfileRequest req) {
        TbUser user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        if (req.getNickname() != null) {
            user.updateNickname(req.getNickname());
        }
    }
    
    @Transactional
    public void changePassword(Long userIdx, ChangePasswordRequest req, String currentToken) {
        TbUser user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        if (user.getDeleteDate() != null || user.getStatus() == TbUser.Status.SUSPENDED) {
            throw CustomException.unauthorized("탈퇴한 계정입니다.");
        }

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPass())) {
            throw CustomException.badRequest("현재 비밀번호가 일치하지 않습니다.");
        }

        user.updatePass(passwordEncoder.encode(req.getNewPassword()));
        
        if (currentToken != null) {
            tokenBlacklist.blacklist(currentToken, jwtService.getExpiresAt(currentToken));
        }
    }
    
    @Transactional
    public void withdraw(Long userIdx) {
        TbUser user = userRepository.findById(userIdx)
            .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        user.withdraw();
    }
}
