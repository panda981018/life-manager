package com.lifemanager.life_manager.dto.user;

import com.lifemanager.life_manager.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.util.stream.Stream;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String name;

    public static UserResponse from(User user) {
        String returnEmail = user.getEmail();
        String provider = user.getProvider();

        if (returnEmail != null && !returnEmail.isEmpty() && provider != null) {
            String lowerProvider = provider.toLowerCase();
            String prefix = lowerProvider + "_";

            if (!"local".equals(lowerProvider) && returnEmail.startsWith(prefix)) {
                returnEmail = returnEmail.substring(prefix.length());
            }
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(returnEmail)
                .name(user.getName())
                .build();
    }

}
