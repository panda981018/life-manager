package com.lifemanager.life_manager.repository;

import com.lifemanager.life_manager.domain.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUserId(Long userId);

    Page<Schedule> findByUserId(Long userId, Pageable pageable);

    // 날짜 범위로 조회
    List<Schedule> findByUserIdAndStartDatetimeBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
