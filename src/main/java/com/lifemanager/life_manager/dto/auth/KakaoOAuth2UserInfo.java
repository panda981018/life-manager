package com.lifemanager.life_manager.dto.auth;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {

    private Map<String, Object> attibutes;

    public KakaoOAuth2UserInfo(Map<String, Object> attibutes) {
        this.attibutes = attibutes;
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attibutes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attibutes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }

        return (String) kakaoAccount.get("email");
    }

    @Override
    @SuppressWarnings("unchecked")
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attibutes.get("properties");
        if (properties == null) {
            return null;
        }

        return (String) properties.get("nickname");
    }
}
