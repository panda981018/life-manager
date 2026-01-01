package com.lifemanager.life_manager.dto.transaction;

import com.lifemanager.life_manager.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.bridge.Message;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class TransactionRequest {

    @NotNull(message = "거래 유형은 필수입니다")
    private TransactionType type;

    @NotNull(message = "금액은 필수입니다")
    @Positive(message = "금액은 0 보다 커야 합니다")
    private BigDecimal amount;

    @NotBlank(message = "카테고리는 필수입니다")
    private String category;

    private String description;

    @NotNull(message = "거래 날짜는 필수입니다")
    private LocalDate transactionDate;

}
