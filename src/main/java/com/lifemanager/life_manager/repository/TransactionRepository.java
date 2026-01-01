package com.lifemanager.life_manager.repository;

import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    @Query("SELECT SUM(t.amount)" +
            " FROM Transaction t" +
            " WHERE t.user.id = :userId" +
            " AND t.type = :type" +
            " AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );

}
