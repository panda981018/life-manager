package com.lifemanager.life_manager.contorller;

import com.lifemanager.life_manager.config.CurrentUserId;
import com.lifemanager.life_manager.dto.transaction.TransactionRequest;
import com.lifemanager.life_manager.dto.transaction.TransactionResponse;
import com.lifemanager.life_manager.dto.transaction.TransactionSummary;
import com.lifemanager.life_manager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @CurrentUserId Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TransactionResponse> response = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
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
