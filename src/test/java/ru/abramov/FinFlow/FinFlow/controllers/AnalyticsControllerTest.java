package ru.abramov.FinFlow.FinFlow.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.abramov.FinFlow.FinFlow.config.JWTFilter;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.*;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.AnalyticsService;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.PersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyticsService analyticsService;

    @MockitoBean
    private AuthPersonService authPersonService;

    @MockitoBean
    private ModelMapper modelMapper;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        person.setName("Arseny");
        when(authPersonService.getCurrentPerson()).thenReturn(person);
    }


    @Test
    @DisplayName("Возвращает текущий баланс пользователя")
    void getCurrentBalance_ShouldReturnBalance() throws Exception {
        when(analyticsService.getCurrentBalance(person)).thenReturn(5000.0);

        mockMvc.perform(get("/analytics/balance/current")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("5000.0"));
    }

    @Test
    @DisplayName("Возвращает историю баланса за указанный период")
    void getBalanceHistory_ShouldReturnHistory() throws Exception {
        BalanceHistoryResponse resp = new BalanceHistoryResponse(); // твоя DTO
        when(analyticsService.getBalanceHistory(eq(1), eq(PeriodUnit.MONTH),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(resp);

        mockMvc.perform(get("/analytics/balance/history")
                        .param("period", "month")
                        .param("start", "2025-10-01")
                        .param("end", "2025-10-31"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(resp)));
    }

    @Test
    @DisplayName("Возвращает 400, если дата конца раньше даты начала")
    void getBalanceHistory_ShouldReturn400_WhenEndBeforeStart() throws Exception {
        mockMvc.perform(get("/analytics/balance/history")
                        .param("period", "month")
                        .param("start", "2025-11-01")
                        .param("end", "2025-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("end must be >= start"));
    }

    @Test
    @DisplayName("Возвращает 400, если указан неизвестный период")
    void getBalanceHistory_ShouldReturn400_WhenUnknownPeriod() throws Exception {
        mockMvc.perform(get("/analytics/balance/history")
                        .param("period", "unknown")
                        .param("start", "2025-10-01")
                        .param("end", "2025-10-31"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unknown period: unknown"));
    }

    @Test
    @DisplayName("Возвращает список расходов по категориям за указанный месяц")
    void getExpensesByCategory_ShouldReturnList() throws Exception {
        List<ExpenseByCategoryDTO> list = List.of(new ExpenseByCategoryDTO("Food", 500.0));
        when(analyticsService.getExpenseByCategory(eq(1), eq(YearMonth.of(2025, 10))))
                .thenReturn(list);

        mockMvc.perform(get("/analytics/expenses/by-category")
                        .param("month", "2025-10"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(list)));
    }

    @Test
    @DisplayName("Возвращает сравнение доходов и расходов за указанный год")
    void getIncomeVsExpenses_ShouldReturnDTO() throws Exception {
        IncomeVsExpensesDTO dto = new IncomeVsExpensesDTO();
        when(analyticsService.getIncomeVsExpenses(2025)).thenReturn(dto);

        mockMvc.perform(get("/analytics/income-vs-expenses")
                        .param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(dto)));
    }

    @Test
    @DisplayName("Возвращает статистику по выбранному месяцу")
    void getMonthlySummary_ShouldReturnDTO() throws Exception {
        MonthStaticDTO dto = new MonthStaticDTO();
        when(analyticsService.getMonthStatic("2025-10")).thenReturn(dto);

        mockMvc.perform(get("/analytics/monthly-summary")
                        .param("month", "2025-10"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(dto)));
    }
}