package com.with.with;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN");
        return repository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/websocket-chat/**"))) // CSRF 검사에서 "/websocket-chat/**" 경로 제외
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/sign-up", "/error", "/check-username", "/check-displayname", "/presigned-url").permitAll()  // 로그인, 회원가입, 에러 페이지는 누구나 접근 가능
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/websocket-chat/**", "/user/queue/**").permitAll()  // WebSocket 경로 권한 허용
                        .anyRequest().authenticated())  // 그 외 모든 요청은 인증 필요
                .formLogin(formLogin -> formLogin
                        .loginPage("/login").defaultSuccessUrl("/my-page"))
                .logout(logout -> logout.logoutUrl("/logout"))
                .rememberMe(rememberMe -> rememberMe
                        .key("uniqueAndSecret").tokenValiditySeconds(86400)); // 24시간 동안 유효

        return http.build();
    }
}
