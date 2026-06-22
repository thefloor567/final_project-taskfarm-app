package com.team4.taskfarm.admin.config;

import com.team4.taskfarm.common.config.JwtFilter;
import com.team4.taskfarm.common.config.JwtService;
import com.team4.taskfarm.common.config.TokenBlacklist;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

/**
 * 어드민앱 보안 설정.
 *
 * 🚧 현재 단계: 전부 허용(permitAll) — 화면 작업용.
 *
 * 🔜 어드민 인증/권한 분리 task(★★★)에서:
 *    - 유저앱과 달리 모든 요청에 ROLE_ADMIN 요구해야 함:
 *        .anyRequest().hasRole("ADMIN")
 *      (JWT role 클레임이 "ROLE_ADMIN"으로 들어오므로 hasRole("ADMIN")과 매칭)
 *    - JwtFilter는 common 것을 그대로 재사용 (user와 같은 필터, 정책만 다름)
 *    - prod에선 추가로 사내 IP 제한 / 노출 최소화
 *    ⚠️ 어드민 권한이 유저앱과 섞이면 보안사고 → 반드시 분리.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JWT role 클레임 값과 정확히 일치하는 권한. hasRole의 암묵적 ROLE_ 프리픽스에 의존하지 않도록 hasAuthority로 명시.
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final JwtService jwtService;
    
    private final TokenBlacklist tokenBlacklist;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 로그인 페이지 + 로그인 API + 정적 리소스는 누구나 접근 가능
                .requestMatchers("/", "/api/login", "/css/**", "/js/**", "/plugins/**", "/media/**", "/webjars/**").permitAll()
                // 나머지는 ROLE_ADMIN 만 (JwtFilter가 넣는 권한 문자열과 정확히 일치)
                .requestMatchers("/actuator/health/**","/actuator/prometheus/**").permitAll()
                .anyRequest().hasAuthority(ROLE_ADMIN)
            )
            .addFilterBefore(new JwtFilter(jwtService, tokenBlacklist), UsernamePasswordAuthenticationFilter.class);

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
