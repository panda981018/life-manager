package com.lifemanager.life_manager.contorller;

import com.lifemanager.life_manager.config.CurrentUserId;
import com.lifemanager.life_manager.domain.Schedule;
import com.lifemanager.life_manager.dto.schedule.ScheduleRequest;
import com.lifemanager.life_manager.dto.schedule.ScheduleResponse;
import com.lifemanager.life_manager.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 일정 생성
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @CurrentUserId Long userId,
            @Valid @RequestBody ScheduleRequest request) {
        Logger logger = Logger.getLogger("[jiwon]");
        ScheduleResponse response = scheduleService.createSchedule(userId, request);
        return ResponseEntity.ok(response);
    }

    // 사용자의 모든 일정 조회
    @GetMapping
    public ResponseEntity<Page<ScheduleResponse>> getAllSchedules(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDatetime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Schedule> schedules = scheduleService.getAllSchedules(userId, pageable);
        Page<ScheduleResponse> response = schedules.map(ScheduleResponse::from);

        return ResponseEntity.ok(response);
    }

    // 기간별 일정 조회
    @GetMapping("/range")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByRange(
            @CurrentUserId Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)LocalDateTime end) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByDateRange(userId, start, end);
        return ResponseEntity.ok(schedules);
    }

    // 일정 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long scheduleId,
            @CurrentUserId Long userId,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(scheduleId, userId, request);
        return ResponseEntity.ok(response);
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            @CurrentUserId Long userId) {
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }
}
