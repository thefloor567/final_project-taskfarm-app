package com.team4.taskfarm.common.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String userId, String role, Long userIdx) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);
        return JWT.create()
                .withSubject(userId)
                .withClaim("role", role)
                .withClaim("userIdx", userIdx)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String getUserId(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getSubject();
    }

    public String getRole(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("role")
                .asString();
    }

    public Long getUserIdx(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token)
                .getClaim("userIdx")
                .asLong();
    }

    public boolean isValid(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    
    public java.util.Date getExpiresAt(String token){
    	return JWT.require(Algorithm.HMAC256(secret))
    			.build()
    			.verify(token)
    			.getExpiresAt();
    }
    
    
    
}
