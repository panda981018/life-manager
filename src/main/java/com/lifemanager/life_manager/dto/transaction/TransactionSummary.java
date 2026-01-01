package com.lifemanager.life_manager.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class TransactionSummary {

    private BigDecimal totalIncome;     // 총 수입
    private BigDecimal totalExpense;    // 총 지출
    private BigDecimal balance;         // 잔액 (총 수입 - 총 지출)
}
