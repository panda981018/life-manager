package com.lifemanager.life_manager.contorller;

import com.lifemanager.life_manager.config.CurrentUserId;
import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.dto.transaction.TransactionRequest;
import com.lifemanager.life_manager.dto.transaction.TransactionResponse;
import com.lifemanager.life_manager.dto.transaction.TransactionSummary;
import com.lifemanager.life_manager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    // 수입/지출 생성
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @CurrentUserId Long userId,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.ok(response);
    }

    // 기간별 거래 내역 조회
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactionsByDateRange(
            @CurrentUserId Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Transaction> transactions = transactionService.getTransactionsByDateRange(
                userId, start, end, pageable
        );
        Page<TransactionResponse> response = transactions.map(TransactionResponse::from);

        return ResponseEntity.ok(response);
    }

    // 기간별 요약 정리
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummary> getSummary(
            @CurrentUserId Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        TransactionSummary summary = transactionService.getSummary(userId, startDate, endDate);
        return ResponseEntity.ok(summary);
    }

    // 거래 수정
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long transactionId,
            @CurrentUserId Long userId,
            @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(transactionId, userId, request);
        return ResponseEntity.ok(response);
    }

    // 거래 삭제
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long transactionId,
            @CurrentUserId Long userId) {
        transactionService.deleteTransaction(transactionId, userId);
        return ResponseEntity.noContent().build();
    }
}
