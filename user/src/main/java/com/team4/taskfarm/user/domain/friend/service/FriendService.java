package com.team4.taskfarm.user.domain.friend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.team4.taskfarm.common.entity.social.TbFriend;
import com.team4.taskfarm.common.entity.social.TbFriend.Status;
import com.team4.taskfarm.common.entity.user.TbUser;
import com.team4.taskfarm.common.exception.CustomException;
import com.team4.taskfarm.user.domain.achievement.service.AchievementService;
import com.team4.taskfarm.user.domain.auth.repository.AuthUserRepository;
import com.team4.taskfarm.user.domain.friend.dto.FriendPageResponseDto;
import com.team4.taskfarm.user.domain.friend.dto.FriendRequestDto;
import com.team4.taskfarm.user.domain.friend.dto.FriendResponseDto;
import com.team4.taskfarm.user.domain.friend.repository.FriendRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final AuthUserRepository authUserRepository;
    private final AchievementService achievementService;

    @Transactional(readOnly = true)
    public FriendPageResponseDto getFriendPage(Long idxUser) {
        TbUser me = authUserRepository.findById(idxUser)
                .orElseThrow(() -> CustomException.notFound("유저를 찾을 수 없습니다."));

        List<TbFriend> all = friendRepository.findByIdxUserAAndStatusOrIdxUserBAndStatus(
                idxUser, Status.ACCEPTED,
                idxUser, Status.ACCEPTED
        );

        List<FriendResponseDto> friends = all.stream()
                .map(friend -> toResponse(friend, idxUser))
                .toList();

        List<TbFriend> pending = friendRepository.findByIdxUserAAndStatusOrIdxUserBAndStatus(
                idxUser, Status.PENDING,
                idxUser, Status.PENDING
        );

        List<FriendResponseDto> received = pending.stream()
                .filter(friend -> !friend.getRequestedBy().equals(idxUser))
                .map(friend -> toResponse(friend, idxUser))
                .toList();

        List<FriendResponseDto> sent = pending.stream()
                .filter(friend -> friend.getRequestedBy().equals(idxUser))
                .map(friend -> toResponse(friend, idxUser))
                .toList();

        return FriendPageResponseDto.builder()
                .myCode(me.getFriendCode())
                .pendingCount(received.size())
                .friends(friends)
                .received(received)
                .sent(sent)
                .build();
    }

    @Transactional
    public FriendResponseDto requestFriend(Long idxUser, FriendRequestDto req) {
        TbUser targetUser = authUserRepository.findByFriendCode(req.getCode())
                .orElseThrow(() -> CustomException.notFound("해당 친구코드를 가진 유저를 찾을 수 없습니다."));

        Long targetIdx = targetUser.getIdxUser();

        if (idxUser.equals(targetIdx)) {
            throw CustomException.badRequest("자기 자신에게 친구 신청을 보낼 수 없습니다.");
        }

        Long idxUserA = Math.min(idxUser, targetIdx);
        Long idxUserB = Math.max(idxUser, targetIdx);

        TbFriend existing = friendRepository.findByIdxUserAAndIdxUserB(idxUserA, idxUserB)
                .orElse(null);

        if (existing != null) {
            if (existing.getStatus() == Status.ACCEPTED) {
                throw CustomException.badRequest("이미 친구입니다.");
            }

            if (!existing.getRequestedBy().equals(idxUser)) {
                existing.accept();

                // 자동 맞수락도 친구 성사 → 업적 체크 (양쪽)
                Long opponent = existing.opponentOf(idxUser);
                try {
                    achievementService.checkAndGrant(idxUser, "friend_count");
                    achievementService.checkAndGrant(opponent, "friend_count");
                } catch (Exception e) {
                    log.warn("친구 업적 체크 실패(무시) - {}", e.getMessage());
                }

                return toResponse(existing, idxUser);
            }

            throw CustomException.badRequest("이미 친구 신청을 보냈습니다.");
        }

        TbFriend friend = TbFriend.request(idxUser, targetIdx);
        TbFriend saved = friendRepository.save(friend);

        return toResponse(saved, idxUser);
    }

    @Transactional
    public FriendResponseDto acceptFriend(Long idxUser, Long idxFriend) {
        TbFriend friend = getFriendOrThrow(idxFriend);
        validateParticipant(friend, idxUser);

        if (friend.getStatus() == Status.ACCEPTED) {
            throw CustomException.badRequest("이미 수락된 친구입니다.");
        }

        if (friend.getRequestedBy().equals(idxUser)) {
            throw CustomException.badRequest("본인이 보낸 친구 신청은 직접 수락할 수 없습니다.");
        }

        friend.accept();

        Long me = idxUser;
        Long opponent = friend.opponentOf(idxUser);
        try {
            achievementService.checkAndGrant(me, "friend_count");
            achievementService.checkAndGrant(opponent, "friend_count");
        } catch (Exception e) {
            log.warn("친구 업적 체크 실패(무시) - {}", e.getMessage());
        }

        return toResponse(friend, idxUser);
    }

    @Transactional
    public void rejectFriend(Long idxUser, Long idxFriend) {
        TbFriend friend = getFriendOrThrow(idxFriend);
        validateParticipant(friend, idxUser);

        if (friend.getStatus() == Status.ACCEPTED) {
            throw CustomException.badRequest("이미 친구인 사용자는 거절할 수 없습니다.");
        }

        if (friend.getRequestedBy().equals(idxUser)) {
            throw CustomException.badRequest("본인이 보낸 친구 신청은 거절할 수 없습니다.");
        }

        friendRepository.delete(friend);
    }

    @Transactional
    public void deleteFriend(Long idxUser, Long idxFriend) {
        TbFriend friend = getFriendOrThrow(idxFriend);
        validateParticipant(friend, idxUser);

        friendRepository.delete(friend);
    }

    private TbFriend getFriendOrThrow(Long idxFriend) {
        return friendRepository.findById(idxFriend)
                .orElseThrow(() -> CustomException.notFound("친구 정보를 찾을 수 없습니다."));
    }

    private void validateParticipant(TbFriend friend, Long idxUser) {
        if (!friend.getIdxUserA().equals(idxUser) && !friend.getIdxUserB().equals(idxUser)) {
            throw CustomException.forbidden("본인과 관련된 친구 정보만 처리할 수 있습니다.");
        }
    }

    private FriendResponseDto toResponse(TbFriend friend, Long myUserIdx) {
        Long opponentIdx = friend.opponentOf(myUserIdx);

        TbUser opponent = authUserRepository.findById(opponentIdx)
                .orElseThrow(() -> CustomException.notFound("상대 유저를 찾을 수 없습니다."));

        return FriendResponseDto.builder()
                .id(friend.getIdxFriend())
                .nickname(opponent.getNickname())
                .code(opponent.getFriendCode())
                .level(opponent.getLevel())
                .rank(null)
                .title(null)
                .build();
    }
}