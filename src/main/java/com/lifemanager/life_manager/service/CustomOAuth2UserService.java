package com.lifemanager.life_manager.service;

import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.auth.*;
import com.lifemanager.life_manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, attributes);

        if (oAuth2UserInfo.getEmail() == null) {
            throw new OAuth2AuthenticationException("이메일을 가져올 수 없습니다");
        }

        User user = saveOrUpdate(oAuth2UserInfo);

        return new CustomOAuth2User(oAuth2User, user);
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            // Google Login

            // Naver Login

            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    private User saveOrUpdate(OAuth2UserInfo oAuth2UserInfo) {
        String email = oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getEmail();

        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setName(oAuth2UserInfo.getName());
        } else {
            user = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .name(oAuth2UserInfo.getName())
                    .provider(oAuth2UserInfo.getProvider())
                    .providerId(oAuth2UserInfo.getProviderId())
                    .build();
            log.info("새로운 OAuth2 사용자 등록 - provider: {}, email: {}",
                    oAuth2UserInfo.getProvider(), oAuth2UserInfo.getEmail());
        }

        return userRepository.save(user);
    }
}
