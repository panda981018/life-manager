package com.lifemanager.life_manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lifemanager.life_manager.domain.TransactionType;
import com.lifemanager.life_manager.domain.User;
import com.lifemanager.life_manager.dto.transaction.TransactionRequest;
import com.lifemanager.life_manager.dto.transaction.TransactionResponse;
import com.lifemanager.life_manager.repository.TransactionRepository;
import com.lifemanager.life_manager.repository.UserRepository;
import com.lifemanager.life_manager.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PasswordEncoder encoder;

    private ObjectMapper objectMapper;
    private User testUser;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = User.builder()
                .email("trans-controller@test.com")
                .password(encoder.encode("password12345"))
                .name("거래컨트롤러테스트")
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("수입 생성 API - 성공")
    @WithMockUser
    void createTransaction_수입_성공() throws Exception {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setAmount(new BigDecimal("1000000"));
        request.setCategory("급여");
        request.setDescription("1월 급여");
        request.setTransactionDate(LocalDate.of(2025, 1, 25));

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.amount").value(1000000))
                .andExpect(jsonPath("$.category").value("급여"))
                .andExpect(jsonPath("$.description").value("1월 급여"))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    @DisplayName("지출 생성 API - 성공")
    @WithMockUser
    void createTransaction_지출_성공() throws Exception {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("50000"));
        request.setCategory("식비");
        request.setDescription("점심 식사");
        request.setTransactionDate(LocalDate.of(2025, 1, 15));

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.amount").value(50000))
                .andExpect(jsonPath("$.category").value("식비"));
    }

    @Test
    @DisplayName("거래 생성 API - 금액 누락 시 400 에러")
    @WithMockUser
    void createTransaction_금액누락_실패() throws Exception {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setCategory("급여");
        request.setTransactionDate(LocalDate.of(2025, 1, 25));

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("거래 생성 API - 카테고리 누락 시 400 에러")
    @WithMockUser
    void createTransaction_카테고리누락_실패() throws Exception {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setAmount(new BigDecimal("100000"));
        request.setTransactionDate(LocalDate.of(2025, 1, 25));

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("거래 생성 API - 금액이 0 이하일 때 400 에러")
    @WithMockUser
    void createTransaction_금액0이하_실패() throws Exception {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("-1000"));
        request.setCategory("식비");
        request.setTransactionDate(LocalDate.of(2025, 1, 15));

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("거래 생성 API - 인증 없이 요청 시 401 에러")
    void createTransaction_인증없음_실패() throws Exception {
        // given
        TransactionRequest request = new TransactionRequest();
        request.setType(TransactionType.INCOME);
        request.setAmount(new BigDecimal("100000"));
        request.setCategory("급여");
        request.setTransactionDate(LocalDate.of(2025, 1, 25));

        // when & then
        mockMvc.perform(post("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("날짜 범위로 거래 내역 조회 API - 성공")
    @WithMockUser
    void getTransactionsByDateRange_성공() throws Exception {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        for (int i = 0; i < 5; i++) {
            TransactionRequest request = new TransactionRequest();
            request.setType(i % 2 == 0 ? TransactionType.INCOME : TransactionType.EXPENSE);
            request.setAmount(new BigDecimal("10000").multiply(new BigDecimal(i + 1)));
            request.setCategory("테스트" + i);
            request.setTransactionDate(baseDate.plusDays(i));
            transactionService.createTransaction(testUser.getId(), request);
        }

        // when & then
        mockMvc.perform(get("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("거래 내역 조회 API - 페이지네이션")
    @WithMockUser
    void getTransactionsByDateRange_페이지네이션_성공() throws Exception {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        for (int i = 0; i < 25; i++) {
            TransactionRequest request = new TransactionRequest();
            request.setType(TransactionType.EXPENSE);
            request.setAmount(new BigDecimal("10000"));
            request.setCategory("테스트" + i);
            request.setTransactionDate(baseDate.plusDays(i % 28));
            transactionService.createTransaction(testUser.getId(), request);
        }

        // when & then - 첫 번째 페이지
        mockMvc.perform(get("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(25))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.content.length()").value(10));

        // when & then - 세 번째 페이지
        mockMvc.perform(get("/api/transactions")
                        .header("X-User-Id", testUser.getId())
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5));
    }

    @Test
    @DisplayName("거래 통계 조회 API - 성공")
    @WithMockUser
    void getSummary_성공() throws Exception {
        // given
        LocalDate baseDate = LocalDate.of(2025, 1, 1);

        // 수입 2건 = 1,500,000원
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
        income2.setTransactionDate(baseDate.plusDays(15));
        transactionService.createTransaction(testUser.getId(), income2);

        // 지출 2건 = 300,000원
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

        // when & then
        mockMvc.perform(get("/api/transactions/summary")
                        .header("X-User-Id", testUser.getId())
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(1500000))
                .andExpect(jsonPath("$.totalExpense").value(300000))
                .andExpect(jsonPath("$.balance").value(1200000));
    }

    @Test
    @DisplayName("거래 통계 조회 API - 거래 없을 때 0 반환")
    @WithMockUser
    void getSummary_거래없음() throws Exception {
        // when & then
        mockMvc.perform(get("/api/transactions/summary")
                        .header("X-User-Id", testUser.getId())
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(0))
                .andExpect(jsonPath("$.totalExpense").value(0))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @DisplayName("거래 수정 API - 성공")
    @WithMockUser
    void updateTransaction_성공() throws Exception {
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

        // when & then
        mockMvc.perform(put("/api/transactions/{transactionId}", created.getId())
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(75000))
                .andExpect(jsonPath("$.category").value("외식"))
                .andExpect(jsonPath("$.description").value("저녁 회식"));
    }

    @Test
    @DisplayName("거래 수정 API - 유형 변경 (지출 -> 수입)")
    @WithMockUser
    void updateTransaction_유형변경_성공() throws Exception {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("100000"));
        createRequest.setCategory("기타");
        createRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setAmount(new BigDecimal("100000"));
        updateRequest.setCategory("환불");
        updateRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        // when & then
        mockMvc.perform(put("/api/transactions/{transactionId}", created.getId())
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("환불"));
    }

    @Test
    @DisplayName("거래 수정 API - 존재하지 않는 거래")
    @WithMockUser
    void updateTransaction_거래없음_실패() throws Exception {
        // given
        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setAmount(new BigDecimal("100000"));
        updateRequest.setCategory("기타");
        updateRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        // when & then
        mockMvc.perform(put("/api/transactions/{transactionId}", 999999L)
                        .header("X-User-Id", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("거래 내역을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("거래 수정 API - 다른 사용자의 거래 수정 시도")
    @WithMockUser
    void updateTransaction_권한없음_실패() throws Exception {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another-trans-ctrl@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자")
                .build();
        anotherUser = userRepository.save(anotherUser);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setType(TransactionType.EXPENSE);
        updateRequest.setAmount(new BigDecimal("100000"));
        updateRequest.setCategory("기타");
        updateRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        // when & then
        mockMvc.perform(put("/api/transactions/{transactionId}", created.getId())
                        .header("X-User-Id", anotherUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 거래 내역을 수정할 권한이 없습니다"));
    }

    @Test
    @DisplayName("거래 삭제 API - 성공")
    @WithMockUser
    void deleteTransaction_성공() throws Exception {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        // when & then
        mockMvc.perform(delete("/api/transactions/{transactionId}", created.getId())
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isNoContent());

        // 삭제 확인
        assertThat(transactionRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("거래 삭제 API - 존재하지 않는 거래")
    @WithMockUser
    void deleteTransaction_거래없음_실패() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/transactions/{transactionId}", 999999L)
                        .header("X-User-Id", testUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("거래 내역을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("거래 삭제 API - 다른 사용자의 거래 삭제 시도")
    @WithMockUser
    void deleteTransaction_권한없음_실패() throws Exception {
        // given
        TransactionRequest createRequest = new TransactionRequest();
        createRequest.setType(TransactionType.EXPENSE);
        createRequest.setAmount(new BigDecimal("50000"));
        createRequest.setCategory("식비");
        createRequest.setTransactionDate(LocalDate.of(2025, 1, 15));

        TransactionResponse created = transactionService.createTransaction(testUser.getId(), createRequest);

        // 다른 사용자 생성
        User anotherUser = User.builder()
                .email("another-trans-ctrl2@test.com")
                .password(encoder.encode("password12345"))
                .name("다른사용자2")
                .build();
        anotherUser = userRepository.save(anotherUser);

        // when & then
        mockMvc.perform(delete("/api/transactions/{transactionId}", created.getId())
                        .header("X-User-Id", anotherUser.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("해당 거래 내역을 삭제할 권한이 없습니다"));

        // 삭제되지 않았는지 확인
        assertThat(transactionRepository.findById(created.getId())).isPresent();
    }
}
