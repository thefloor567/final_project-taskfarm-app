package com.team4.taskfarm.user.domain.user.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.team4.taskfarm.common.entity.user.TbUser;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class UserResponse {
    private Long idxUser;
    private String email;
    private String nickname;
    private int level;
    private int exp;
    private long joinDays;

    public static UserResponse from(TbUser user) {
    	
    	long days = 0;
    	if (user.getCreateDate() != null) {
    		days = ChronoUnit.DAYS.between(
    				user.getCreateDate().toLocalDate(), LocalDate.now()) + 1;
    				
    	}
    	
    	
        return UserResponse.builder()
            .idxUser(user.getIdxUser())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .level(user.getLevel())
            .exp(user.getExp())
            .joinDays(days)
            .build();
    }
}