package com.lifemanager.life_manager.repository;

import com.lifemanager.life_manager.domain.Schedule;
import com.lifemanager.life_manager.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ScheduleRepositoryTest {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("schedule-repo@test.com")
                .password("password12345")
                .name("일정레포테스트")
                .build();
        testUser = userRepository.save(testUser);

        anotherUser = User.builder()
                .email("another-repo@test.com")
                .password("password12345")
                .name("다른사용자")
                .build();
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    @DisplayName("사용자 ID로 일정 목록 조회")
    void findByUserId_성공() {
        // given
        Schedule schedule1 = Schedule.builder()
                .user(testUser)
                .title("일정 1")
                .startDatetime(LocalDateTime.now())
                .endDatetime(LocalDateTime.now().plusHours(1))
                .isAllDay(false)
                .build();

        Schedule schedule2 = Schedule.builder()
                .user(testUser)
                .title("일정 2")
                .startDatetime(LocalDateTime.now().plusDays(1))
                .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
                .isAllDay(false)
                .build();

        // 다른 사용자의 일정
        Schedule otherSchedule = Schedule.builder()
                .user(anotherUser)
                .title("다른 사용자 일정")
                .startDatetime(LocalDateTime.now())
                .endDatetime(LocalDateTime.now().plusHours(1))
                .isAllDay(false)
                .build();

        scheduleRepository.save(schedule1);
        scheduleRepository.save(schedule2);
        scheduleRepository.save(otherSchedule);

        // when
        List<Schedule> schedules = scheduleRepository.findByUserId(testUser.getId());

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules).extracting(Schedule::getTitle)
                .containsExactlyInAnyOrder("일정 1", "일정 2");
    }

    @Test
    @DisplayName("사용자 ID로 일정 페이지네이션 조회")
    void findByUserId_페이지네이션_성공() {
        // given
        for (int i = 0; i < 15; i++) {
            Schedule schedule = Schedule.builder()
                    .user(testUser)
                    .title("일정 " + i)
                    .startDatetime(LocalDateTime.now().plusDays(i))
                    .endDatetime(LocalDateTime.now().plusDays(i).plusHours(1))
                    .isAllDay(false)
                    .build();
            scheduleRepository.save(schedule);
        }

        Pageable firstPage = PageRequest.of(0, 10);
        Pageable secondPage = PageRequest.of(1, 10);

        // when
        Page<Schedule> firstPageResult = scheduleRepository.findByUserId(testUser.getId(), firstPage);
        Page<Schedule> secondPageResult = scheduleRepository.findByUserId(testUser.getId(), secondPage);

        // then
        assertThat(firstPageResult.getTotalElements()).isEqualTo(15);
        assertThat(firstPageResult.getTotalPages()).isEqualTo(2);
        assertThat(firstPageResult.getContent()).hasSize(10);
        assertThat(secondPageResult.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("날짜 범위로 일정 조회 - 범위 내 일정만 반환")
    void findByUserIdAndStartDatetimeBetween_성공() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2025, 6, 1, 10, 0);

        // 범위 내 일정 (6월 1일 ~ 6월 15일)
        Schedule inRange1 = Schedule.builder()
                .user(testUser)
                .title("범위 내 일정 1")
                .startDatetime(baseTime)
                .endDatetime(baseTime.plusHours(1))
                .isAllDay(false)
                .build();

        Schedule inRange2 = Schedule.builder()
                .user(testUser)
                .title("범위 내 일정 2")
                .startDatetime(baseTime.plusDays(7))
                .endDatetime(baseTime.plusDays(7).plusHours(1))
                .isAllDay(false)
                .build();

        // 범위 외 일정 (6월 20일)
        Schedule outOfRange = Schedule.builder()
                .user(testUser)
                .title("범위 외 일정")
                .startDatetime(baseTime.plusDays(19))
                .endDatetime(baseTime.plusDays(19).plusHours(1))
                .isAllDay(false)
                .build();

        // 다른 사용자의 범위 내 일정
        Schedule otherUserInRange = Schedule.builder()
                .user(anotherUser)
                .title("다른 사용자 범위 내 일정")
                .startDatetime(baseTime.plusDays(5))
                .endDatetime(baseTime.plusDays(5).plusHours(1))
                .isAllDay(false)
                .build();

        scheduleRepository.save(inRange1);
        scheduleRepository.save(inRange2);
        scheduleRepository.save(outOfRange);
        scheduleRepository.save(otherUserInRange);

        // when
        List<Schedule> schedules = scheduleRepository.findByUserIdAndStartDatetimeBetween(
                testUser.getId(),
                baseTime.minusDays(1),
                baseTime.plusDays(15)
        );

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules).extracting(Schedule::getTitle)
                .containsExactlyInAnyOrder("범위 내 일정 1", "범위 내 일정 2");
    }

    @Test
    @DisplayName("날짜 범위에 해당하는 일정이 없으면 빈 리스트 반환")
    void findByUserIdAndStartDatetimeBetween_결과없음() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2025, 6, 1, 10, 0);

        Schedule schedule = Schedule.builder()
                .user(testUser)
                .title("6월 일정")
                .startDatetime(baseTime)
                .endDatetime(baseTime.plusHours(1))
                .isAllDay(false)
                .build();
        scheduleRepository.save(schedule);

        // when - 7월 범위로 조회
        List<Schedule> schedules = scheduleRepository.findByUserIdAndStartDatetimeBetween(
                testUser.getId(),
                baseTime.plusMonths(1),
                baseTime.plusMonths(2)
        );

        // then
        assertThat(schedules).isEmpty();
    }

    @Test
    @DisplayName("일정이 없는 사용자 조회 시 빈 리스트 반환")
    void findByUserId_일정없음() {
        // when
        List<Schedule> schedules = scheduleRepository.findByUserId(testUser.getId());

        // then
        assertThat(schedules).isEmpty();
    }
}
