package com.lifemanager.life_manager.repository;

import com.lifemanager.life_manager.domain.Transaction;
import com.lifemanager.life_manager.domain.TransactionType;
import com.lifemanager.life_manager.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("transaction-repo@test.com")
                .password("password12345")
                .name("거래레포테스트")
                .build();
        testUser = userRepository.save(testUser);

        anotherUser = User.builder()
                .email("another-trans-repo@test.com")
                .password("password12345")
                .name("다른사용자")
                .build();
        anotherUser = userRepository.save(anotherUser);
    }

    @Test
    @DisplayName("날짜 범위로 거래 내역 조회 - 리스트")
    void findByUserIdAndTransactionDateBetween_리스트_성공() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        // 범위 내 거래
        Transaction inRange1 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .category("급여")
                .transactionDate(baseDate)
                .build();

        Transaction inRange2 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50000"))
                .category("식비")
                .transactionDate(baseDate.plusDays(10))
                .build();

        // 범위 외 거래
        Transaction outOfRange = Transaction.builder()
                .user(testUser)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("30000"))
                .category("기타")
                .transactionDate(baseDate.plusMonths(2))
                .build();

        // 다른 사용자 거래
        Transaction otherUser = Transaction.builder()
                .user(anotherUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("200000"))
                .category("급여")
                .transactionDate(baseDate.plusDays(5))
                .build();

        transactionRepository.save(inRange1);
        transactionRepository.save(inRange2);
        transactionRepository.save(outOfRange);
        transactionRepository.save(otherUser);

        // when
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                testUser.getId(),
                baseDate,
                baseDate.plusDays(15)
        );

        // then
        assertThat(transactions).hasSize(2);
        assertThat(transactions).extracting(Transaction::getCategory)
                .containsExactlyInAnyOrder("급여", "식비");
    }

    @Test
    @DisplayName("날짜 범위로 거래 내역 조회 - 페이지네이션")
    void findByUserIdAndTransactionDateBetween_페이지네이션_성공() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        for (int i = 0; i < 25; i++) {
            Transaction transaction = Transaction.builder()
                    .user(testUser)
                    .type(i % 2 == 0 ? TransactionType.INCOME : TransactionType.EXPENSE)
                    .amount(new BigDecimal("10000").multiply(new BigDecimal(i + 1)))
                    .category("테스트" + i)
                    .transactionDate(baseDate.plusDays(i))
                    .build();
            transactionRepository.save(transaction);
        }

        Pageable firstPage = PageRequest.of(0, 10);
        Pageable thirdPage = PageRequest.of(2, 10);

        // when
        Page<Transaction> firstPageResult = transactionRepository.findByUserIdAndTransactionDateBetween(
                testUser.getId(),
                baseDate,
                baseDate.plusDays(30),
                firstPage
        );

        Page<Transaction> thirdPageResult = transactionRepository.findByUserIdAndTransactionDateBetween(
                testUser.getId(),
                baseDate,
                baseDate.plusDays(30),
                thirdPage
        );

        // then
        assertThat(firstPageResult.getTotalElements()).isEqualTo(25);
        assertThat(firstPageResult.getTotalPages()).isEqualTo(3);
        assertThat(firstPageResult.getContent()).hasSize(10);
        assertThat(thirdPageResult.getContent()).hasSize(5);
    }

    @Test
    @DisplayName("수입 합계 계산 - JPQL 쿼리")
    void sumAmountByUserIdAndTypeAndDateBetween_수입합계() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        Transaction income1 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("1000000"))
                .category("급여")
                .transactionDate(baseDate)
                .build();

        Transaction income2 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500000"))
                .category("보너스")
                .transactionDate(baseDate.plusDays(15))
                .build();

        // 지출 (합계에 포함되면 안 됨)
        Transaction expense = Transaction.builder()
                .user(testUser)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("200000"))
                .category("월세")
                .transactionDate(baseDate.plusDays(5))
                .build();

        // 범위 외 수입 (합계에 포함되면 안 됨)
        Transaction outOfRangeIncome = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("300000"))
                .category("기타수입")
                .transactionDate(baseDate.plusMonths(2))
                .build();

        transactionRepository.save(income1);
        transactionRepository.save(income2);
        transactionRepository.save(expense);
        transactionRepository.save(outOfRangeIncome);

        // when
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.INCOME,
                baseDate,
                baseDate.plusDays(20)
        );

        // then
        assertThat(totalIncome).isEqualByComparingTo(new BigDecimal("1500000"));
    }

    @Test
    @DisplayName("지출 합계 계산 - JPQL 쿼리")
    void sumAmountByUserIdAndTypeAndDateBetween_지출합계() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        Transaction expense1 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("200000"))
                .category("월세")
                .transactionDate(baseDate)
                .build();

        Transaction expense2 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("100000"))
                .category("식비")
                .transactionDate(baseDate.plusDays(10))
                .build();

        Transaction expense3 = Transaction.builder()
                .user(testUser)
                .type(TransactionType.EXPENSE)
                .amount(new BigDecimal("50000"))
                .category("교통비")
                .transactionDate(baseDate.plusDays(15))
                .build();

        // 수입 (합계에 포함되면 안 됨)
        Transaction income = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("1000000"))
                .category("급여")
                .transactionDate(baseDate.plusDays(5))
                .build();

        transactionRepository.save(expense1);
        transactionRepository.save(expense2);
        transactionRepository.save(expense3);
        transactionRepository.save(income);

        // when
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.EXPENSE,
                baseDate,
                baseDate.plusDays(20)
        );

        // then
        assertThat(totalExpense).isEqualByComparingTo(new BigDecimal("350000"));
    }

    @Test
    @DisplayName("거래 내역이 없을 때 합계는 0 반환 (COALESCE)")
    void sumAmountByUserIdAndTypeAndDateBetween_거래없음_0반환() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        // when
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.INCOME,
                baseDate,
                baseDate.plusDays(30)
        );

        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.EXPENSE,
                baseDate,
                baseDate.plusDays(30)
        );

        // then
        assertThat(totalIncome).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(totalExpense).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("다른 사용자의 거래는 합계에 포함되지 않음")
    void sumAmountByUserIdAndTypeAndDateBetween_사용자격리() {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        Transaction myIncome = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("1000000"))
                .category("급여")
                .transactionDate(baseDate)
                .build();

        Transaction otherIncome = Transaction.builder()
                .user(anotherUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("2000000"))
                .category("급여")
                .transactionDate(baseDate)
                .build();

        transactionRepository.save(myIncome);
        transactionRepository.save(otherIncome);

        // when
        BigDecimal myTotal = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.INCOME,
                baseDate,
                baseDate.plusDays(30)
        );

        BigDecimal otherTotal = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                anotherUser.getId(),
                TransactionType.INCOME,
                baseDate,
                baseDate.plusDays(30)
        );

        // then
        assertThat(myTotal).isEqualByComparingTo(new BigDecimal("1000000"));
        assertThat(otherTotal).isEqualByComparingTo(new BigDecimal("2000000"));
    }

    @Test
    @DisplayName("경계값 테스트 - 시작일과 종료일 포함 확인")
    void sumAmountByUserIdAndTypeAndDateBetween_경계값() {
        // given
        LocalDate startDate = LocalDate.of(2025, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 1, 31);

        // 시작일 거래
        Transaction onStartDate = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("100000"))
                .category("시작일")
                .transactionDate(startDate)
                .build();

        // 종료일 거래
        Transaction onEndDate = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("200000"))
                .category("종료일")
                .transactionDate(endDate)
                .build();

        // 범위 외 (시작일 전날)
        Transaction beforeStart = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("50000"))
                .category("시작전")
                .transactionDate(startDate.minusDays(1))
                .build();

        // 범위 외 (종료일 다음날)
        Transaction afterEnd = Transaction.builder()
                .user(testUser)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("50000"))
                .category("종료후")
                .transactionDate(endDate.plusDays(1))
                .build();

        transactionRepository.save(onStartDate);
        transactionRepository.save(onEndDate);
        transactionRepository.save(beforeStart);
        transactionRepository.save(afterEnd);

        // when
        BigDecimal total = transactionRepository.sumAmountByUserIdAndTypeAndDateBetween(
                testUser.getId(),
                TransactionType.INCOME,
                startDate,
                endDate
        );

        // then - 시작일과 종료일 거래만 포함
        assertThat(total).isEqualByComparingTo(new BigDecimal("300000"));
    }
}
