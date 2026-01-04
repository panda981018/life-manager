package com.lifemanager.life_manager.repository;

import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.domain.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // 기존 메서드 - 날짜 범위로 조회
    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    // 페이지네이션 메서드 추가
    Page<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // Summary 계산 - 총 수입
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "AND t.type = :type")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}