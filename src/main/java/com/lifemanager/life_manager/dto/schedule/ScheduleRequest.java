package com.lifemanager.life_manager.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScheduleRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    private String description;

    @NotNull(message = "시작 시간은 필수입니다")
    private LocalDateTime startDatetime;

    @NotNull(message = "종료 시간은 필수입니다")
    private LocalDateTime endDatetime;

    private Boolean isAllDay = false;

    private String category;

    private String color;

}
