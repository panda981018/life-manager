package com.lifemanager.life_manager.contorller;

import com.lifemanager.life_manager.config.CurrentUserId;
import com.lifemanager.life_manager.dto.schedule.ScheduleRequest;
import com.lifemanager.life_manager.dto.schedule.ScheduleResponse;
import com.lifemanager.life_manager.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @CurrentUserId Long userId) {
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByUser(userId);
        return ResponseEntity.ok(schedules);
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
