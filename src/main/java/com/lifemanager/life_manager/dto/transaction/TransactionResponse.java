package com.lifemanager.life_manager.dto.transaction;

import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.domain.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate transactionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity -> DTO 변환 메서드
    public static TransactionResponse from(Transaction trans) {
        return TransactionResponse.builder()
                .id(trans.getId())
                .type(trans.getType())
                .amount(trans.getAmount())
                .category(trans.getCategory())
                .description(trans.getDescription())
                .transactionDate(trans.getTransactionDate())
                .createdAt(trans.getCreatedAt())
                .updatedAt(trans.getUpdatedAt())
                .build();
    }
}
