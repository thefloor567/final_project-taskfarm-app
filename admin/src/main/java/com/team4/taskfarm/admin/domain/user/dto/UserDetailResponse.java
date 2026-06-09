package com.team4.taskfarm.admin.domain.user.dto;

import com.team4.taskfarm.common.entity.user.TbUser;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public class UserDetailResponse {
    private final Long userId;
    private final String nickname;
    private final String email;
    private final int level;
    private final String joinDate;
    private final long doneCount;
    private final double completionRate;   // 0.0 ~ 100.0
    private final int totalXp;
    private final long cropCount;
    private final List<String> activityLabels;
    private final List<Long> activityData;

    public UserDetailResponse(TbUser u, long doneCount, double completionRate,
                              long cropCount, List<String> activityLabels, List<Long> activityData) {
        this.userId = u.getIdxUser();
        this.nickname = u.getNickname();
        this.email = u.getEmail();
        this.level = u.getLevel();
        this.joinDate = u.getCreateDate() != null
                ? u.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : "-";
        this.doneCount = doneCount;
        this.completionRate = completionRate;
        this.totalXp = u.getExp();
        this.cropCount = cropCount;
        this.activityLabels = activityLabels;
        this.activityData = activityData;
    }
}
