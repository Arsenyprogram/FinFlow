package ru.abramov.FinFlow.FinFlow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.*;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionSavedDTO;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;
import ru.abramov.FinFlow.FinFlow.repository.CategoryRepository;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthPersonService authPersonService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Person person;

    @BeforeEach
    void setUp(){
        person = new Person();
        person.setId(1);
    }


    @Test
    void getCurrentBalance_shouldReturnCorrectBalance() {
        Person person = new Person();
        person.setId(1);

        List<Transaction> transactions = List.of(
                Transaction.builder().type("INCOME").amount(100.0).build(),
                Transaction.builder().type("EXPENSE").amount(40.0).build(),
                Transaction.builder().type("INCOME").amount(60.0).build()
        );

        when(transactionRepository.findAllByUserId(1)).thenReturn(transactions);

        double result = analyticsService.getCurrentBalance(person);

        assertEquals(120.0, result);
    }


    @ParameterizedTest
    @EnumSource(PeriodUnit.class)
    void getBalanceHistory_shouldAccumulateCorrectly(PeriodUnit periodUnit) {
        int userId = 1;
        LocalDate start = LocalDate.of(2025, 10, 1);
        LocalDate end = LocalDate.of(2025, 10, 10);

        // starting balance
        when(transactionRepository.sumBefore(userId, start, "INCOME", "EXPENSE"))
                .thenReturn(BigDecimal.valueOf(100));

        // daily transactions
        List<DateSumProjection> dailySums = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDate date = start.plusDays(i);
            BigDecimal sum = BigDecimal.valueOf(i * 10); // 0, 10, 20, ..., 90
            DateSumProjection dsp = mock(DateSumProjection.class);
            when(dsp.getDate()).thenReturn(date);
            when(dsp.getSum()).thenReturn(sum);
            dailySums.add(dsp);
        }
        when(transactionRepository.sumByDateBetween(userId, start, end, "INCOME", "EXPENSE"))
                .thenReturn(dailySums);

        // вызов метода
        BalanceHistoryResponse result = analyticsService.getBalanceHistory(userId, periodUnit, start, end);

        // проверка начального баланса
        assertEquals(BigDecimal.valueOf(100), result.getStartingBalance());

        // проверка окончания баланса
        BigDecimal expectedTotalChange = dailySums.stream()
                .map(DateSumProjection::getSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(BigDecimal.valueOf(100).add(expectedTotalChange), result.getEndingBalance());

        // проверка точек
        switch (periodUnit) {
            case DAY -> assertEquals(10, result.getPoints().size());
            case WEEK -> assertEquals(2, result.getPoints().size()); // 1–7, 8–10
            case MONTH -> assertEquals(1, result.getPoints().size());
            case YEAR -> assertEquals(1, result.getPoints().size());
        }

        // можно дополнительно проверить первую точку для всех периодов
        BalancePointDTO first = result.getPoints().get(0);
        assertNotNull(first.getStart());
        assertNotNull(first.getEnd());
        assertNotNull(first.getBalance());
        assertNotNull(first.getChange());
    }


    @Test
    void getExpenseByCategory_shouldReturnList() {
        int userId = 1;
        YearMonth month = YearMonth.of(2025, 10);

        ExpenseByCategoryDTO dto = new ExpenseByCategoryDTO();
        dto.setCategory("Food");
        dto.setTotal(new BigDecimal("200"));

        when(transactionRepository.findExpensesByCategory(userId, "EXPENSE",
                month.atDay(1), month.atEndOfMonth()))
                .thenReturn(List.of(dto));

        List<ExpenseByCategoryDTO> result = analyticsService.getExpenseByCategory(userId, month);

        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getCategory());
        assertEquals(new BigDecimal("200"), result.get(0).getTotal());
    }


    @Test
    void getIncomeVsExpenses_shouldCombineIncomeAndExpense() {
        when(authPersonService.getCurrentPerson()).thenReturn(person);

        var income = mock(MonthlySumProjection.class);
        when(income.getMonth()).thenReturn("01");
        when(income.getSum()).thenReturn(1000.0);

        var expense1 = mock(MonthlySumProjection.class);
        when(expense1.getMonth()).thenReturn("01");
        when(expense1.getSum()).thenReturn(400.0);

        var expense2 = mock(MonthlySumProjection.class);
        when(expense2.getMonth()).thenReturn("02");
        when(expense2.getSum()).thenReturn(200.0);

        when(transactionRepository.getMonthlyIncome(2025, person.getId()))
                .thenReturn(List.of(income));
        when(transactionRepository.getMonthlyExpenses(2025, person.getId()))
                .thenReturn(List.of(expense1, expense2));

        IncomeVsExpensesDTO result = analyticsService.getIncomeVsExpenses(2025);

        assertEquals(2025, result.getYear());
        assertEquals(2, result.getMonthlyData().size());
        assertEquals(1000.0, result.getMonthlyData().get(0).getIncome());
        assertEquals(400.0, result.getMonthlyData().get(0).getExpenses());
    }


    @Test
    void getMonthStatic_shouldReturnCorrectStatistics() {
        when(authPersonService.getCurrentPerson()).thenReturn(person);

        YearMonth ym = YearMonth.of(2025, 10);
        String ymString = ym.toString();

        when(transactionRepository.getMonthlyIncome(
                String.valueOf(ym.getYear()), "10", person.getId()))
                .thenReturn(new BigDecimal("1000"));

        when(transactionRepository.getMonthlyExpenses(
                String.valueOf(ym.getYear()), "10", person.getId()))
                .thenReturn(new BigDecimal("600"));

        when(transactionRepository.findMostExpensiveCategory(ymString, person.getId()))
                .thenReturn(List.of("Food"));

        Transaction tx = new Transaction();
        tx.setId(1L);

        when(transactionRepository.findLargestTransaction(ymString, person.getId(), PageRequest.of(0, 1)))
                .thenReturn(List.of(tx));

        TransactionSavedDTO dto = new TransactionSavedDTO();
        dto.setCategoryId(5L);
        when(modelMapper.map(tx, TransactionSavedDTO.class)).thenReturn(dto);

        when(categoryRepository.findById(5L))
                .thenReturn(Optional.of(Category.builder().id(5).name("Food").build()));

        MonthStaticDTO result = analyticsService.getMonthStatic(ymString);

        assertEquals("10", result.getMonth());
        assertEquals(new BigDecimal("1000"), result.getTotalIncome());
        assertEquals(new BigDecimal("600"), result.getTotalExpense());
        assertEquals("Food", result.getMostExpensiveCategory());
    }

}