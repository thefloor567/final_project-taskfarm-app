package com.team4.taskfarm.common.config;

import java.util.Date;

public interface TokenBlacklist {
	
	void blacklist(String token, Date expiresAt);
	
	boolean isBlacklisted(String token);
}
