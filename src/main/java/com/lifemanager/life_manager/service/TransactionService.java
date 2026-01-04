package com.lifemanager.life_manager.service;

import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.domain.TransactionType;
import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.transaction.TransactionRequest;
import com.lifemanager.life_manager.dto.transaction.TransactionResponse;
import com.lifemanager.life_manager.dto.transaction.TransactionSummary;
import com.lifemanager.life_manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    // 수입 또는 지출 기록
    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = userService.findById(userId);

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(request.getType())
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        return TransactionResponse.from(saved);
    }

    // 기간 내 수입/지출 기록 조회
    public Page<Transaction> getTransactionsByDateRange(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startDate, endDate, pageable
        );
    }

    // 수입/지출 통계
    public TransactionSummary getSummary(Long userId, LocalDate startDate, LocalDate endDate) {
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(userId, TransactionType.EXPENSE, startDate, endDate);

        // null 체크
        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;

        BigDecimal balance = totalIncome.subtract(totalExpense);

        return TransactionSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .build();
    }

    // 수입/지출 수정
    @Transactional
    public TransactionResponse updateTransaction(Long transactionId, Long userId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다"));

        // 권한 체크
        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 거래 내역을 수정할 권한이 없습니다");
        }

        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());

        return TransactionResponse.from(transaction);
    }

    // 수입/지출 내역 삭제
    @Transactional
    public void deleteTransaction(Long transactionId, Long userId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("거래 내역을 찾을 수 없습니다"));

        // 권한 체크
        if (!transaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("해당 거래 내역을 삭제할 권한이 없습니다");
        }

        transactionRepository.delete(transaction);
    }
}
