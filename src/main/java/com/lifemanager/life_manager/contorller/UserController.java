package com.lifemanager.life_manager.contorller;

import com.lifemanager.life_manager.config.CurrentUserId;
import com.lifemanager.life_manager.dto.user.PasswordChangeRequest;
import com.lifemanager.life_manager.dto.user.UserResponse;
import com.lifemanager.life_manager.dto.user.UserUpdateRequest;
import com.lifemanager.life_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getUser(@CurrentUserId Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    // 사용자 정보 수정 (이름)
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            @CurrentUserId Long userId,
            @RequestBody UserUpdateRequest request
    ) {
        UserResponse updatedUser = userService.updateUser(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    // 비밀번호 변경
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @CurrentUserId Long userId,
            @RequestBody PasswordChangeRequest request
    ) {
        userService.changePassword(userId, request);
        return ResponseEntity.ok("비밀번호가 변경되었습니다");
    }
}
