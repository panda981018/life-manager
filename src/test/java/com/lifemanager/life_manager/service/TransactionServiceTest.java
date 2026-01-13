package com.lifemanager.life_manager.service;

import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.domain.TransactionType;
import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.transaction.TransactionRequest;
import com.lifemanager.life_manager.dto.transaction.TransactionResponse;
import com.lifemanager.life_manager.dto.transaction.TransactionSummary;
import com.lifemanager.life_manager.repository.TransactionRepository;
import com.lifemanager.life_manager.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class TransactionServiceTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder encoder;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("transaction@test.com")
                .password(encoder.encode("password12345"))
                .name("거래테스트")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("수입 기록 생성 성공")
    void 수입기록생성_성공() {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setAmount(new BigDecimal("1000000"));
        request.setCategory("급여");
        request.setDescription("1월 급여");
        request.setTransactionDate(LocalDate.of(2025, 1, 25));

        // when
        TransactionResponse response = transactionService.createTransaction(testUser.getId(), request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(response.getCategory()).isEqualTo("급여");
        assertThat(response.getDescription()).isEqualTo("1월 급여");
        assertThat(response.getTransactionDate()).isEqualTo(LocalDate.of(2025, 1, 25));
    }

    @Test
    @DisplayName("지출 기록 생성 성공")
    void 지출기록생성_성공() {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("50000"));
        request.setCategory("식비");
        request.setDescription("점심 식사");
        request.setTransactionDate(LocalDate.of(2025, 1, 15));

        // when
        TransactionResponse response = transactionService.createTransaction(testUser.getId(), request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(response.getCategory()).isEqualTo("식비");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 거래 생성 시 실패")
    void 거래생성_실패_사용자없음() {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setAmount(new BigDecimal("100000"));
        request.setCategory("기타");
        request.setTransactionDate(LocalDate.now());

        // when & then
        assertThatThrownBy(() -> transactionService.createTransaction(999999L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("날짜 범위로 거래 내역 조회 - 페이지네이션")
    void 거래내역조회_날짜범위_성공() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        // 범위 내 거래 3개
        for (int i = 0; i < 3; i++) {
            TransactionRequest request = new TransactionRequest();
            request.setType(i % 2 == 0 ? TransactionType.INCOME : TransactionType.EXPENSE);
            request.setAmount(new BigDecimal("10000").multiply(new BigDecimal(i + 1)));
            request.setCategory("테스트" + i);
            request.setTransactionDate(baseDate.plusDays(i * 5));
            transactionService.createTransaction(testUser.getId(), request);
        }

        // 범위 외 거래 1개
        TransactionRequest outOfRangeRequest = new TransactionRequest();
        outOfRangeRequest.setType(TransactionType.EXPENSE);
        outOfRangeRequest.setAmount(new BigDecimal("50000"));
        outOfRangeRequest.setCategory("범위외");
        outOfRangeRequest.setTransactionDate(baseDate.plusMonths(3));
        transactionService.createTransaction(testUser.getId(), outOfRangeRequest);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Transaction> transactions = transactionService.getTransactionsByDateRange(
                testUser.getId(),
                baseDate,
                baseDate.plusDays(20),
                pageable
        );

        // then
        assertThat(transactions.getTotalElements()).isEqualTo(3);
        assertThat(transactions.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("수입/지출 통계 조회 - 잔액 계산")
    void 통계조회_성공() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        // 수입 2건 총 150만원
        TransactionRequest income1 = new TransactionRequest();
        income1.setType(TransactionType.INCOME);
        income1.setAmount(new BigDecimal("1000000"));
        income1.setCategory("급여");
        income1.setTransactionDate(baseDate);
        transactionService.createTransaction(testUser.getId(), income1);

        TransactionRequest income2 = new TransactionRequest();
        income2.setType(TransactionType.INCOME);
        income2.setAmount(new BigDecimal("500000"));
        income2.setCategory("보너스");
        income2.setTransactionDate(baseDate.plusDays(5));
        transactionService.createTransaction(testUser.getId(), income2);

        // 지출 2건 총 30만원
        TransactionRequest expense1 = new TransactionRequest();
        expense1.setType(TransactionType.EXPENSE);
        expense1.setAmount(new BigDecimal("200000"));
        expense1.setCategory("월세");
        expense1.setTransactionDate(baseDate.plusDays(1));
        transactionService.createTransaction(testUser.getId(), expense1);

        TransactionRequest expense2 = new TransactionRequest();
        expense2.setType(TransactionType.EXPENSE);
        expense2.setAmount(new BigDecimal("100000"));
        expense2.setCategory("식비");
        expense2.setTransactionDate(baseDate.plusDays(10));
        transactionService.createTransaction(testUser.getId(), expense2);

        // when
        TransactionSummary summary = transactionService.getSummary(
                testUser.getId(),
                baseDate,
                baseDate.plusDays(15)
        );

        // then
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("1500000"));
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(new BigDecimal("300000"));
        assertThat(summary.getBalance()).isEqualByComparingTo(new BigDecimal("1200000"));
    }

    @Test
    @DisplayName("거래 내역이 없을 때 통계 조회 - 0 반환")
    void 통계조회_거래없음() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        // when
        TransactionSummary summary = transactionService.getSummary(
                testUser.getId(),
                baseDate,
                baseDate.plusDays(30)
        );

        // then
        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("거래 내역 수정 성공")
    void 거래수정_성공() {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setDescription("점심");
        createRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setAmount(new BigDecimal("75000"));
        updateRequest.setCategory("외식");
        updateRequest.setDescription("저녁 회식");
        updateRequest.setTransactionDate(LocalDate.of(2025, 1, 16));

        // when
        TransactionResponse updated = transactionService.updateTransaction(created.getId(), testUser.getId(), updateRequest);

        // then
        assertThat(updated.getAmount()).isEqualByComparingTo(new BigDecimal("75000"));
        assertThat(updated.getCategory()).isEqualTo("외식");
        assertThat(updated.getDescription()).isEqualTo("저녁 회식");
        assertThat(updated.getTransactionDate()).isEqualTo(LocalDate.of(2025, 1, 16));
    }

    @Test
    @DisplayName("거래 유형 변경 (지출 -> 수입)")
    void 거래유형변경_성공() {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("100000"));
        createRequest.setCategory("기타");
        createRequest.setTransactionDate(LocalDate.now());

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setAmount(new BigDecimal("100000"));
        updateRequest.setCategory("환불");
        updateRequest.setTransactionDate(LocalDate.now());

        // when
        TransactionResponse updated = transactionService.updateTransaction(created.getId(), testUser.getId(), updateRequest);

        // then
        assertThat(updated.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(updated.getCategory()).isEqualTo("환불");
    }

    @Test
    @DisplayName("존재하지 않는 거래 수정 시 실패")
    void 거래수정_실패_거래없음() {
        // given
        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setAmount(new BigDecimal("100000"));
        updateRequest.setCategory("기타");
        updateRequest.setTransactionDate(LocalDate.now());

        // when & then
        assertThatThrownBy(() -> transactionService.updateTransaction(999999L, testUser.getId(), updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래 내역을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 거래 수정 시 권한 없음 실패")
    void 거래수정_실패_권한없음() {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setTransactionDate(LocalDate.now());

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another.trans@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자")
                .build();
        anotherUser = userRepository.save(anotherUser);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setAmount(new BigDecimal("100000"));
        updateRequest.setCategory("기타");
        updateRequest.setTransactionDate(LocalDate.now());

        Long anotherUserId = anotherUser.getId();

        // when & then
        assertThatThrownBy(() -> transactionService.updateTransaction(created.getId(), anotherUserId, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 거래 내역을 수정할 권한이 없습니다");
    }

    @Test
    @DisplayName("거래 삭제 성공")
    void 거래삭제_성공() {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setTransactionDate(LocalDate.now());

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        // when
        transactionService.deleteTransaction(created.getId(), testUser.getId());

        // then
        assertThat(transactionRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 거래 삭제 시 실패")
    void 거래삭제_실패_거래없음() {
        // when & then
        assertThatThrownBy(() -> transactionService.deleteTransaction(999999L, testUser.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("거래 내역을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 사용자의 거래 삭제 시 권한 없음 실패")
    void 거래삭제_실패_권한없음() {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setTransactionDate(LocalDate.now());

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another.trans2@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자2")
                .build();
        anotherUser = userRepository.save(anotherUser);

        Long anotherUserId = anotherUser.getId();

        // when & then
        assertThatThrownBy(() -> transactionService.deleteTransaction(created.getId(), anotherUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 거래 내역을 삭제할 권한이 없습니다");
    }

    @Test
    @DisplayName("삭제 후 통계에 반영되는지 확인")
    void 거래삭제후_통계반영() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        TransactionRequest income = new TransactionRequest();
        income.setType(TransactionType.INCOME);
        income.setAmount(new BigDecimal("1000000"));
        income.setCategory("급여");
        income.setTransactionDate(baseDate);
        transactionService.createTransaction(testUser.getId(), income);

        TransactionRequest expense = new TransactionRequest();
        expense.setType(TransactionType.EXPENSE);
        expense.setAmount(new BigDecimal("200000"));
        expense.setCategory("식비");
        expense.setTransactionDate(baseDate.plusDays(1));
        TransactionResponse createdExpense = transactionService.createTransaction(testUser.getId(), expense);

        // 삭제 전 통계 확인
        TransactionSummary beforeDelete = transactionService.getSummary(testUser.getId(), baseDate, baseDate.plusDays(10));
        assertThat(beforeDelete.getTotalExpense()).isEqualByComparingTo(new BigDecimal("200000"));

        // when - 지출 삭제
        transactionService.deleteTransaction(createdExpense.getId(), testUser.getId());

        // then - 삭제 후 통계 확인
        TransactionSummary afterDelete = transactionService.getSummary(testUser.getId(), baseDate, baseDate.plusDays(10));
        assertThat(afterDelete.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(afterDelete.getTotalIncome()).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(afterDelete.getBalance()).isEqualByComparingTo(new BigDecimal("1000000"));
    }
}
