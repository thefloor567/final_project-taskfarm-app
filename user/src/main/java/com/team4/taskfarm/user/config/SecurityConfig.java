package com.team4.taskfarm.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * 유저앱 보안 설정.
 *
 * 🚧 현재 단계: 퍼블리싱/화면 작업 중이므로 "전부 허용(permitAll)".
 *    Security 의존성이 있으면 기본적으로 모든 요청에 로그인을 요구하기 때문에,
 *    화면을 막힘 없이 확인하려면 여기서 명시적으로 다 열어둠.
 *
 * 🔜 JWT 인증 task(★★★) 들어가면:
 *    1) sessionManagement STATELESS 로 변경
 *    2) authorizeHttpRequests 에서 로그인/회원가입만 permitAll, 나머지 authenticated
 *    3) addFilterBefore(new JwtFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
 *    4) 인증 실패 시 401 JSON 반환하는 authenticationEntryPoint 추가
 *    → Foldy의 SecurityConfig 완성본을 그대로 참고하면 됨 (이미 검증된 코드).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // 🚧 임시: 전부 허용 (화면 작업 단계)
                .anyRequest().permitAll()
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
