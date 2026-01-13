package com.lifemanager.life_manager.service;

import com.lifemanager.life_manager.domain.Schedule;
import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.schedule.ScheduleRequest;
import com.lifemanager.life_manager.dto.schedule.ScheduleResponse;
import com.lifemanager.life_manager.repository.ScheduleRepository;
import com.lifemanager.life_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ScheduleServiceTest {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private PasswordEncoder encoder;

    private User testUser;

    // 테스트 시작 전 실행
    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("schedule@test.com")
                .password(encoder.encode("password12345"))
                .name("일정테스트")
                .build();
        testUser = userRepository.save(this.testUser);
    }

    @Test
    void 일정생성_성공() {
        // given
        ScheduleRequest request = new ScheduleRequest();
        request.setTitle("테스트 일정");
        request.setDescription("테스트 설명");
        request.setStartDatetime(LocalDateTime.now());
        request.setEndDatetime(LocalDateTime.now().plusMinutes(3));
        request.setIsAllDay(false);
        request.setCategory("업무");
        request.setColor("#3B82F6");

        // when
        ScheduleResponse schedule = scheduleService.createSchedule(testUser.getId(), request);


        // then
        assertThat(schedule.getId()).isNotNull();
        assertThat(schedule.getTitle()).isEqualTo("테스트 일정");
        assertThat(schedule.getDescription()).isEqualTo("테스트 설명");
        assertThat(schedule.getCategory()).isEqualTo("업무");
        assertThat(schedule.getColor()).isEqualTo("#3B82F6");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 일정 생성 시 실패")
    void 일정생성_실패_사용자없음() {
        // given
        ScheduleRequest request = new ScheduleRequest();
        request.setTitle("테스트 일정1");
        request.setDescription("테스트 설명1");
        request.setStartDatetime(LocalDateTime.now());
        request.setEndDatetime(LocalDateTime.now().plusMinutes(3));
        request.setIsAllDay(false);
        request.setCategory("업무");
        request.setColor("#3B82F6");

        // when & then
        assertThatThrownBy(() -> scheduleService.createSchedule(999999L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("전체 일정 조회 - 페이지네이션")
    void 일정_전체조회_성공() {
        // given
        for (int i = 0; i < 5; i++) {
            ScheduleRequest request = new ScheduleRequest();
            request.setTitle("일정 " + i);
            request.setDescription("설명 " + i);
            request.setStartDatetime(LocalDateTime.now().plusDays(i));
            request.setEndDatetime(LocalDateTime.now().plusDays(i).plusHours(1));
            request.setIsAllDay(false);
            request.setCategory("업무");
            request.setColor("#3B82F6");
            scheduleService.createSchedule(testUser.getId(), request);
        }

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Schedule> schedules = scheduleService.getAllSchedules(testUser.getId(), pageable);

        // then
        assertThat(schedules.getTotalElements()).isEqualTo(5);
        assertThat(schedules.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("날짜 범위로 일정 조회")
    void 일정_날짜범위조회_성공() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 15, 10, 0);

        // 범위 내 일정 2개
        ScheduleRequest request1 = new ScheduleRequest();
        request1.setTitle("범위 내 일정 1");
        request1.setStartDatetime(baseTime);
        request1.setEndDatetime(baseTime.plusHours(1));
        request1.setIsAllDay(false);
        scheduleService.createSchedule(testUser.getId(), request1);

        ScheduleRequest request2 = new ScheduleRequest();
        request2.setTitle("범위 내 일정 2");
        request2.setStartDatetime(baseTime.plusDays(5));
        request2.setEndDatetime(baseTime.plusDays(5).plusHours(1));
        request2.setIsAllDay(false);
        scheduleService.createSchedule(testUser.getId(), request2);

        // 범위 외 일정 1개
        ScheduleRequest request3 = new ScheduleRequest();
        request3.setTitle("범위 외 일정");
        request3.setStartDatetime(baseTime.plusMonths(2));
        request3.setEndDatetime(baseTime.plusMonths(2).plusHours(1));
        request3.setIsAllDay(false);
        scheduleService.createSchedule(testUser.getId(), request3);

        // when
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDateRange(
                testUser.getId(),
                baseTime.minusDays(1),
                baseTime.plusDays(10)
        );

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules).extracting(ScheduleResponse::getTitle)
                .containsExactlyInAnyOrder("범위 내 일정 1", "범위 내 일정 2");
    }

    @Test
    @DisplayName("일정 수정 성공")
    void 일정수정_성공() {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("원본 일정");
        createRequest.setDescription("원본 설명");
        createRequest.setStartDatetime(LocalDateTime.now());
        createRequest.setEndDatetime(LocalDateTime.now().plusHours(1));
        createRequest.setIsAllDay(false);
        createRequest.setCategory("업무");
        createRequest.setColor("#3B82F6");

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        ScheduleRequest updateRequest = new ScheduleRequest();
        updateRequest.setTitle("수정된 일정");
        updateRequest.setDescription("수정된 설명");
        updateRequest.setStartDatetime(LocalDateTime.now().plusDays(1));
        updateRequest.setEndDatetime(LocalDateTime.now().plusDays(1).plusHours(2));
        updateRequest.setIsAllDay(true);
        updateRequest.setCategory("개인");
        updateRequest.setColor("#EF4444");

        // when
        ScheduleResponse updated = scheduleService.updateSchedule(created.getId(), testUser.getId(), updateRequest);

        // then
        assertThat(updated.getTitle()).isEqualTo("수정된 일정");
        assertThat(updated.getDescription()).isEqualTo("수정된 설명");
        assertThat(updated.getIsAllDay()).isTrue();
        assertThat(updated.getCategory()).isEqualTo("개인");
        assertThat(updated.getColor()).isEqualTo("#EF4444");
    }

    @Test
    @DisplayName("존재하지 않는 일정 수정 시 실패")
    void 일정수정_실패_일정없음() {
        // given
        ScheduleRequest updateRequest = new ScheduleRequest();
        updateRequest.setTitle("수정된 일정");
        updateRequest.setStartDatetime(LocalDateTime.now());
        updateRequest.setEndDatetime(LocalDateTime.now().plusHours(1));
        updateRequest.setIsAllDay(false);

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(999999L, testUser.getId(), updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("일정을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 일정 수정 시 권한 없음 실패")
    void 일정수정_실패_권한없음() {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("원본 일정");
        createRequest.setStartDatetime(LocalDateTime.now());
        createRequest.setEndDatetime(LocalDateTime.now().plusHours(1));
        createRequest.setIsAllDay(false);

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자")
                .build();
        anotherUser = userRepository.save(anotherUser);

        ScheduleRequest updateRequest = new ScheduleRequest();
        updateRequest.setTitle("수정된 일정");
        updateRequest.setStartDatetime(LocalDateTime.now());
        updateRequest.setEndDatetime(LocalDateTime.now().plusHours(1));
        updateRequest.setIsAllDay(false);

        Long anotherUserId = anotherUser.getId();

        // when & then
        assertThatThrownBy(() -> scheduleService.updateSchedule(created.getId(), anotherUserId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 일정을 수정할 권한이 없습니다");
    }

    @Test
    @DisplayName("일정 삭제 성공")
    void 일정삭제_성공() {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("삭제할 일정");
        createRequest.setStartDatetime(LocalDateTime.now());
        createRequest.setEndDatetime(LocalDateTime.now().plusHours(1));
        createRequest.setIsAllDay(false);

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        // when
        scheduleService.deleteSchedule(created.getId(), testUser.getId());

        // then
        assertThat(scheduleRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 일정 삭제 시 실패")
    void 일정삭제_실패_일정없음() {
        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(999999L, testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("일정을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 일정 삭제 시 권한 없음 실패")
    void 일정삭제_실패_권한없음() {
        // given
        ScheduleRequest createRequest = new ScheduleRequest();
        createRequest.setTitle("삭제할 일정");
        createRequest.setStartDatetime(LocalDateTime.now());
        createRequest.setEndDatetime(LocalDateTime.now().plusHours(1));
        createRequest.setIsAllDay(false);

        ScheduleResponse created = scheduleService.createSchedule(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another2@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자2")
                .build();
        anotherUser = userRepository.save(anotherUser);

        Long anotherUserId = anotherUser.getId();

        // when & then
        assertThatThrownBy(() -> scheduleService.deleteSchedule(created.getId(), anotherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 일정을 삭제할 권한이 없습니다");
    }
}