package com.lifemanager.life_manager.dto.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeRequest {

    private String currentPassword;
    private String newPassword;

}
