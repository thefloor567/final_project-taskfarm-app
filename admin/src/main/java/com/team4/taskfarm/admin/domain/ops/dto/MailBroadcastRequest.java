package com.team4.taskfarm.admin.domain.ops.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MailBroadcastRequest {
    private String targetType;          // ALL | USER
    private List<Long> targetUserIds;   // targetType=USER일 때
    @NotBlank(message = "제목을 입력해주세요.") private String title;
    private String body;
    private String rewardType;          // NONE | COIN | TITLE
    @Min(value = 0, message = "코인은 0 이상이어야 합니다") private int rewardCoin;
    private String rewardTitle;
    private String refKey;
}
