package com.team4.taskfarm.user.domain.friend.repository;

import com.team4.taskfarm.common.entity.social.TbFriend;
import com.team4.taskfarm.common.entity.social.TbFriend.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<TbFriend, Long> {

    Optional<TbFriend> findByIdxUserAAndIdxUserB(Long idxUserA, Long idxUserB);

    List<TbFriend> findByIdxUserAAndStatusOrIdxUserBAndStatus(
            Long idxUserA,
            Status statusA,
            Long idxUserB,
            Status statusB
    );

    long countByIdxUserAAndStatusOrIdxUserBAndStatus(
            Long idxUserA,
            Status statusA,
            Long idxUserB,
            Status statusB
    );
}