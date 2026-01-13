package com.lifemanager.life_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.schedule.ScheduleRequest;
import com.lifemanager.life_manager.dto.schedule.ScheduleResponse;
import com.lifemanager.life_manager.repository.ScheduleRepository;
import com.lifemanager.life_manager.repository.UserRepository;
import com.lifemanager.life_manager.service.ScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private PasswordEncoder encoder;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = User.builder()
                .email("controller@test.com")
                .password(encoder.encode("password12345"))
                .name("컨트롤러테스트")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("일정 생성 API - 성공")
    @WithMockUser
    void createSchedule_성공() throws Exception {
        // given
        ScheduleRequest request = new ScheduleRequest();
        request.setTitle("새 일정");
        request.setDescription("테스트 설명");
        request.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        request.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        request.setIsAllDay(false);
        request.setCategory("업무");
        request.setColor("#3B82F6");

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("새 일정"))
                .andExpect(jsonPath("$.description").value("테스트 설명"))
                .andExpect(jsonPath("$.category").value("업무"))
                .andExpect(jsonPath("$.color").value("#3B82F6"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("일정 생성 API - 제목 누락 시 400 에러")
    @WithMockUser
    void createSchedule_제목누락_실패() throws Exception {
        // given
        ScheduleRequest request = new ScheduleRequest();
        request.setDescription("테스트 설명");
        request.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        request.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        request.setIsAllDay(false);

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일정 생성 API - 시작시간 누락 시 400 에러")
    @WithMockUser
    void createSchedule_시작시간누락_실패() throws Exception {
        // given
        ScheduleRequest request = new ScheduleRequest();
        request.setTitle("새 일정");
        request.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        request.setIsAllDay(false);

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("일정 생성 API - 인증 없이 요청 시 401 에러")
    void createSchedule_인증없음_실패() throws Exception {
        // given
        ScheduleRequest request = new ScheduleRequest();
        request.setTitle("새 일정");
        request.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        request.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        request.setIsAllDay(false);

        // when & then
        mockMvc.perform(post("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("전체 일정 조회 API - 성공")
    @WithMockUser
    void getAllSchedules_성공() throws Exception {
        // given
        for (int i = 0; i < 3; i++) {
            ScheduleRequest request = new ScheduleRequest();
            request.setTitle("일정 " + i);
            request.setStartDatetime(LocalDateTime.now().plusDays(i));
            request.setEndDatetime(LocalDateTime.now().plusDays(i).plusHours(1));
            request.setIsAllDay(false);
            scheduleService.createSchedule(testUser.getId(), request);
        }

        // when & then
        mockMvc.perform(get("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("전체 일정 조회 API - 페이지네이션")
    @WithMockUser
    void getAllSchedules_페이지네이션_성공() throws Exception {
        // given
        for (int i = 0; i < 15; i++) {
            ScheduleRequest request = new ScheduleRequest();
            request.setTitle("일정 " + i);
            request.setStartDatetime(LocalDateTime.now().plusDays(i));
            request.setEndDatetime(LocalDateTime.now().plusDays(i).plusHours(1));
            request.setIsAllDay(false);
            scheduleService.createSchedule(testUser.getId(), request);
        }

        // when & then - 첫 번째 페이지
        mockMvc.perform(get("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(15))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.content.length()").value(10));

        // when & then - 두 번째 페이지
        mockMvc.perform(get("/api/schedules")
                        .header("X-User-Id", testUser.getId())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("날짜 범위로 일정 조회 API - 성공")
    @WithMockUser
    void getSchedulesByRange_성공() throws Exception {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2025, 6, 1, 10, 0);

        ScheduleRequest request1 = new ScheduleRequest();
        request1.setTitle("6월 일정 1");
        request1.setStartDatetime(baseTime);
        request1.setEndDatetime(baseTime.plusHours(1));
        request1.setIsAllDay(false);
        scheduleService.createSchedule(testUser.getId(), request1);

        ScheduleRequest request2 = new ScheduleRequest();
        request2.setTitle("6월 일정 2");
        request2.setStartDatetime(baseTime.plusDays(5));
        request2.setEndDatetime(baseTime.plusDays(5).plusHours(1));
        request2.setIsAllDay(false);
        scheduleService.createSchedule(testUser.getId(), request2);

        // when & then
        mockMvc.perform(get("/api/schedules/range")
                        .header("X-User-Id", testUser.getId())
                        .param("start", "2025-06-01T00:00:00")
                        .param("end", "2025-06-15T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("일정 수정 API - 성공")
    @WithMockUser
    void updateSchedule_성공() throws Exception {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("원본 일정");
        createRequest.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        createRequest.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        createRequest.setIsAllDay(false);
        createRequest.setCategory("업무");

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        ScheduleRequest updateRequest = new ScheduleRequest();
        updateRequest.setTitle("수정된 일정");
        updateRequest.setStartDatetime(LocalDateTime.of(2025, 1, 16, 14, 0));
        updateRequest.setEndDatetime(LocalDateTime.of(2025, 1, 16, 16, 0));
        updateRequest.setIsAllDay(true);
        updateRequest.setCategory("개인");
        updateRequest.setColor("#EF4444");

        // when & then
        mockMvc.perform(put("/api/schedules/{scheduleId}", created.getId())
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 일정"))
                .andExpect(jsonPath("$.isAllDay").value(true))
                .andExpect(jsonPath("$.category").value("개인"))
                .andExpect(jsonPath("$.color").value("#EF4444"));
    }

    @Test
    @DisplayName("일정 수정 API - 존재하지 않는 일정")
    @WithMockUser
    void updateSchedule_일정없음_실패() throws Exception {
        // given
        ScheduleRequest updateRequest = new ScheduleRequest();
        updateRequest.setTitle("수정된 일정");
        updateRequest.setStartDatetime(LocalDateTime.of(2025, 1, 16, 14, 0));
        updateRequest.setEndDatetime(LocalDateTime.of(2025, 1, 16, 16, 0));
        updateRequest.setIsAllDay(false);

        // when & then
        mockMvc.perform(put("/api/schedules/{scheduleId}", 999999L)
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("일정을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("일정 수정 API - 다른 사용자의 일정 수정 시도")
    @WithMockUser
    void updateSchedule_권한없음_실패() throws Exception {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("원본 일정");
        createRequest.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        createRequest.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        createRequest.setIsAllDay(false);

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another-ctrl@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자")
                .build();
        anotherUser = userRepository.save(anotherUser);

        ScheduleRequest updateRequest = new ScheduleRequest();
        updateRequest.setTitle("수정된 일정");
        updateRequest.setStartDatetime(LocalDateTime.of(2025, 1, 16, 14, 0));
        updateRequest.setEndDatetime(LocalDateTime.of(2025, 1, 16, 16, 0));
        updateRequest.setIsAllDay(false);

        // when & then
        mockMvc.perform(put("/api/schedules/{scheduleId}", created.getId())
                        .header("X-User-Id", anotherUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 일정을 수정할 권한이 없습니다"));
    }

    @Test
    @DisplayName("일정 삭제 API - 성공")
    @WithMockUser
    void deleteSchedule_성공() throws Exception {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("삭제할 일정");
        createRequest.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        createRequest.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        createRequest.setIsAllDay(false);

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        // when & then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", created.getId())
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isNoContent());

        // 삭제 확인
        assertThat(scheduleRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("일정 삭제 API - 존재하지 않는 일정")
    @WithMockUser
    void deleteSchedule_일정없음_실패() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", 999999L)
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("일정을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("일정 삭제 API - 다른 사용자의 일정 삭제 시도")
    @WithMockUser
    void deleteSchedule_권한없음_실패() throws Exception {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("삭제할 일정");
        createRequest.setStartDatetime(LocalDateTime.of(2025, 1, 15, 10, 0));
        createRequest.setEndDatetime(LocalDateTime.of(2025, 1, 15, 11, 0));
        createRequest.setIsAllDay(false);

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another-ctrl2@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자2")
                .build();
        anotherUser = userRepository.save(anotherUser);

        // when & then
        mockMvc.perform(delete("/api/schedules/{scheduleId}", created.getId())
                        .header("X-User-Id", anotherUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 일정을 삭제할 권한이 없습니다"));

        // 삭제되지 않았는지 확인
        assertThat(scheduleRepository.findById(created.getId())).isPresent();
    }
}
