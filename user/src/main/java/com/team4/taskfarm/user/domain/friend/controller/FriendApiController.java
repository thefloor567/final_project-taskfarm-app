package com.team4.taskfarm.user.domain.friend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.team4.taskfarm.common.response.ApiResponse;
import com.team4.taskfarm.user.common.UserBaseController;
import com.team4.taskfarm.user.domain.friend.dto.FriendPageResponseDto;
import com.team4.taskfarm.user.domain.friend.dto.FriendRequestDto;
import com.team4.taskfarm.user.domain.friend.dto.FriendResponseDto;
import com.team4.taskfarm.user.domain.friend.service.FriendService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendApiController extends UserBaseController {

    private final FriendService friendService;

    @GetMapping
    public ResponseEntity<ApiResponse<FriendPageResponseDto>> getFriends() {
        return ok(friendService.getFriendPage(getCurrentUserIdx()));
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<FriendResponseDto>> requestFriend(
            @Valid @RequestBody FriendRequestDto req
    ) {
        return ok(friendService.requestFriend(getCurrentUserIdx(), req));
    }

    @PostMapping("/{idxFriend}/accept")
    public ResponseEntity<ApiResponse<FriendResponseDto>> acceptFriend(
            @PathVariable Long idxFriend
    ) {
        return ok(friendService.acceptFriend(getCurrentUserIdx(), idxFriend));
    }

    @PostMapping("/{idxFriend}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectFriend(
            @PathVariable Long idxFriend
    ) {
        friendService.rejectFriend(getCurrentUserIdx(), idxFriend);
        return ok();
    }

    @DeleteMapping("/{idxFriend}")
    public ResponseEntity<ApiResponse<Void>> deleteFriend(
            @PathVariable Long idxFriend
    ) {
        friendService.deleteFriend(getCurrentUserIdx(), idxFriend);
        return ok();
    }
}