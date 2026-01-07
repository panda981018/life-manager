package com.lifemanager.life_manager.dto.auth;


public interface OAuth2UserInfo {
    String getProviderId();
    String getProvider();
    String getEmail();
    String getName();
}
