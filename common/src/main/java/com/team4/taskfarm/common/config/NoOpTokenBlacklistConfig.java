package com.team4.taskfarm.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class NoOpTokenBlacklistConfig {
	
	@Bean
	@ConditionalOnMissingBean(TokenBlacklist.class)
	public TokenBlacklist noOpTokenBlacklist() {
		return new TokenBlacklist() {
			@Override public void blacklist(String token, Date expiresAt) {}
			@Override public boolean isBlacklisted(String token) { return false; }
		};
	}
	
}
