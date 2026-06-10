package com.team4.taskfarm.user.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.user.dto.ChangePasswordRequest;
import com.team4.taskfarm.user.domain.user.dto.UpdateProfileRequest;
import com.team4.taskfarm.user.domain.user.dto.UserResponse;
import com.team4.taskfarm.user.domain.user.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController extends UserBaseController {

	private final UserService userService;
	
	@GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        return ok(userService.getProfile(getCurrentUserIdx()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@RequestBody UpdateProfileRequest req) {
        userService.updateProfile(getCurrentUserIdx(), req);
        return ok();
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            HttpServletRequest request) {
        userService.changePassword(getCurrentUserIdx(), req, resolveToken(request));
        return ok();
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw() {
        userService.withdraw(getCurrentUserIdx());
        return ok();
    }
    
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) return bearer.substring(7);
        if (request.getCookies() != null) {
            for (var c : request.getCookies()) {
                if ("token".equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }
}
