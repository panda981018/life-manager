package com.lifemanager.life_manager.repository;

import com.lifemanager.life_manager.domain.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserIdAndStartDatetimeBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Schedule> findByUserIdOrderByStartDatetimeAsc(Long userId);
}
