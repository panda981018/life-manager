package com.lifemanager.life_manager.dto.schedule;

import com.lifemanager.life_manager.domain.Schedule;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleResponse {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private Boolean isAllDay;
    private String category;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 메서드
    public static ScheduleResponse from(Schedule schedule) {
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .startDatetime(schedule.getStartDatetime())
                .endDatetime(schedule.getEndDatetime())
                .isAllDay(schedule.getIsAllDay())
                .category(schedule.getCategory())
                .color(schedule.getColor())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }
}
