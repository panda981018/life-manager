package com.lifemanager.life_manager.contorller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifemanager.life_manager.dto.auth.LoginRequest;
import com.lifemanager.life_manager.dto.auth.SignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void 회원가입_성공() throws Exception {
        // Given
        SignupRequest request = new SignupRequest();
        request.setEmail("signup@example.com");
        request.setPassword("password1234");
        request.setName("testUser");

        // When
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.name").value("testUser"));
    }

    @Test
    void 회원가입_중복이메일_실패() throws Exception {
        // Given 1
        SignupRequest firstRequest = new SignupRequest();
        firstRequest.setEmail("duplicate@example.com");
        firstRequest.setPassword("password1234");
        firstRequest.setName("firstUser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // when
        SignupRequest secondRequest = new SignupRequest();
        secondRequest.setEmail("duplicate@example.com");
        secondRequest.setPassword("password1234");
        secondRequest.setName("secondUser");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(secondRequest)))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_성공() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("login@example.com");
        signupRequest.setPassword("password1234");
        signupRequest.setName("로그인테스트");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // when
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("password1234");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.name").value("로그인테스트"));
    }

    @Test
    void 로그인_잘못된비밀번호_실패() throws Exception {
        // given
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail("wrongpassword@example.com");
        signupRequest.setPassword("password1234");
        signupRequest.setName("로그인테스트");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // when
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("wrongpassword@example.com");
        loginRequest.setPassword("password5678");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
                // then
                .andExpect(status().isBadRequest());
    }
}