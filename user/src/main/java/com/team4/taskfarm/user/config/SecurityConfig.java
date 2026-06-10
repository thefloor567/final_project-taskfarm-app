package com.team4.taskfarm.user.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.team4.taskfarm.common.config.JwtFilter;
import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.config.TokenBlacklist;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    
    private final TokenBlacklist tokenBlacklist;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // 화면 페이지는 일단 허용
                .requestMatchers(
                    "/",
                    "/login",
                    "/signup",
                    "/home",
                    "/todo/**",
                    "/category",
                    "/farm/**",
                    "/stats/**",
                    "/mypage"
                ).permitAll()

                // 정적 리소스 허용
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/plugins/**",
                    "/assets/**",
                    "/favicon.ico"
                ).permitAll()
                
                // 헬스체크 (EKS liveness/readiness) + 에러 포워딩
                .requestMatchers("/actuator/health/**", "/error").permitAll()

                // 인증 API 중 로그인/회원가입만 허용
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/signup"
                ).permitAll()

                // 나머지 API는 JWT 필요
                .requestMatchers("/api/**").authenticated()

                // 나머지는 일단 허용
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                new JwtFilter(jwtService, tokenBlacklist),
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}