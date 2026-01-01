package com.lifemanager.life_manager.service;

import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.auth.AuthResponse;
import com.lifemanager.life_manager.dto.auth.LoginRequest;
import com.lifemanager.life_manager.dto.auth.SignupRequest;
import com.lifemanager.life_manager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 (가입 완료되면 자동 로그인)
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        // UserService를 통해 회원가입
        User user = userService.signup(request);

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());

        // 응답 생성
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    // 로그인
    public AuthResponse login(LoginRequest request) {
        // 사용자 조회
        User user = userService.findByEmail(request.getEmail());

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user.getEmail(), user.getId());

        // 응답 생성
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
