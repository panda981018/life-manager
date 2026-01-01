package com.lifemanager.life_manager.service;

import com.lifemanager.life_manager.domain.Schedule;
import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.schedule.ScheduleRequest;
import com.lifemanager.life_manager.dto.schedule.ScheduleResponse;
import com.lifemanager.life_manager.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserService userService;

    // 스케줄 생성
    @Transactional
    public ScheduleResponse createSchedule(Long userId, ScheduleRequest request) {
        User user = userService.findById(userId);

        Schedule schedule = Schedule.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .startDatetime(request.getStartDatetime())
                .endDatetime(request.getEndDatetime())
                .isAllDay(request.getIsAllDay())
                .category(request.getCategory())
                .color(request.getColor())
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        return ScheduleResponse.from(saved);

    }

    // UserId에 맞는 스케줄 리스트 전체 가져오기
    public List<ScheduleResponse> getSchedulesByUser(Long userId) {
        return scheduleRepository.findByUserIdOrderByStartDatetimeAsc(userId)
                .stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    // UserId에 맞으면서 날짜 범위에 해당하는 스케줄을 조회
    public List<ScheduleResponse> getSchedulesByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByUserIdAndStartDatetimeBetween(userId, start, end)
                .stream()
                .map(ScheduleResponse::from)
                .collect(Collectors.toList());
    }

    // 일정 수정
    @Transactional
    public ScheduleResponse updateSchedule(Long scheduleId, Long userId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 일정을 수정할 권한이 없습니다");
        }

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setStartDatetime(request.getStartDatetime());
        schedule.setEndDatetime(request.getEndDatetime());
        schedule.setIsAllDay(request.getIsAllDay());
        schedule.setCategory(request.getCategory());
        schedule.setColor(request.getColor());

        return ScheduleResponse.from(schedule);
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다"));

        // 권한 체크
        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 일정을 삭제할 권한이 없습니다");
        }

        scheduleRepository.delete(schedule);
    }
}
