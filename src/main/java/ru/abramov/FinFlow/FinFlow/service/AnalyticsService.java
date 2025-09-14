package ru.abramov.FinFlow.FinFlow.service;

import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.*;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionSavedDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;
import ru.abramov.FinFlow.FinFlow.repository.CategoryRepository;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {


    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AuthPersonService authPersonService;

    private final ModelMapper modelMapper;

    @Autowired
    public AnalyticsService( TransactionRepository transactionRepository, TransactionService transactionService, CategoryRepository categoryRepository, AuthPersonService authPersonService, ModelMapper modelMapper) {

        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.authPersonService = authPersonService;

        this.modelMapper = modelMapper;
    }

    public double getCurrentBalance(Person person) {
        Double transactionExpense = transactionRepository.findAllByUserId(person.getId()).stream()
                .filter(transaction -> transaction.getType().equals("EXPENSE"))
                .mapToDouble(Transaction::getAmount)
                .sum();
        Double transactionIncome = transactionRepository.findAllByUserId(person.getId()).stream()
                .filter(transaction -> transaction.getType().equals("INCOME"))
                .mapToDouble(Transaction::getAmount)
                .sum();
        return transactionIncome - transactionExpense;
    }


    public BalanceHistoryResponse getBalanceHistory(int userId, PeriodUnit periodUnit, LocalDate start, LocalDate end){
        if(end.isBefore(start)){
            throw new IllegalArgumentException("end must be >= start");
        }
        BigDecimal startingBalance = transactionRepository.sumBefore(userId, start, "INCOME", "EXPENSE");
        if(startingBalance == null){
            startingBalance = BigDecimal.ZERO;
        }
        List<DateSumProjection> daile = transactionRepository.sumByDateBetween(userId, start, end, "INCOME", "EXPENSE");
        Map<LocalDate, BigDecimal> dailyMap = daile.stream()
                .collect(Collectors.toMap(DateSumProjection::getDate, sum ->
                        sum.getSum() == null ? BigDecimal.ZERO: sum.getSum()));

        List<PeriodRange> ranges = buildRanges(periodUnit, start, end);

        List<BalancePointDTO> points = new ArrayList<>();
        BigDecimal currentBalance = startingBalance;
        for (PeriodRange r : ranges) {
            BigDecimal periodSum = sumForRange(dailyMap, r.start, r.end);
            currentBalance = currentBalance.add(periodSum);

            BalancePointDTO dto = new BalancePointDTO();
            dto.setLabel(labelForRange(periodUnit, r.start, r.end));
            dto.setStart(r.start);
            dto.setEnd(r.end);
            dto.setChange(periodSum);
            dto.setBalance(currentBalance);

            points.add(dto);
        }

        BalanceHistoryResponse resp = new BalanceHistoryResponse();
        resp.setStartingBalance(startingBalance);
        resp.setPoints(points);
        resp.setEndingBalance(currentBalance);
        return resp;


    }

    public List<ExpenseByCategoryDTO> getExpenseByCategory(int userId, YearMonth month){
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return transactionRepository.findExpensesByCategory(userId, "EXPENSE", start, end);
    }


    private String labelForRange(PeriodUnit unit, LocalDate start, LocalDate end) {
        return switch (unit) {
            case DAY -> start.toString();
            case WEEK -> start + " — " + end;
            case MONTH -> start.getYear() + "-" + String.format("%02d", start.getMonthValue());
            case YEAR -> String.valueOf(start.getYear());
        };
    }



    private BigDecimal sumForRange(Map<LocalDate, BigDecimal> dailyMap, LocalDate start, LocalDate end) {
        BigDecimal sum = BigDecimal.ZERO;
        LocalDate d = start;
        while (!d.isAfter(end)) {
            sum = sum.add(dailyMap.getOrDefault(d, BigDecimal.ZERO));
            d = d.plusDays(1);
        }
        return sum;
    }


    private List<PeriodRange> buildRanges(PeriodUnit unit, LocalDate start, LocalDate end) {
        List<PeriodRange> ranges = new ArrayList<>();
        LocalDate cur = start;

        while (!cur.isAfter(end)) {
            LocalDate periodEnd;
            switch (unit) {
                case DAY -> periodEnd = cur;
                case WEEK -> periodEnd = cur.plusDays(6);
                case MONTH -> periodEnd = cur.withDayOfMonth(cur.lengthOfMonth());
                case YEAR -> periodEnd = cur.withDayOfYear(cur.lengthOfYear());
                default -> periodEnd = cur;
            }
            if (periodEnd.isAfter(end)) periodEnd = end;
            ranges.add(new PeriodRange(cur, periodEnd));
            cur = periodEnd.plusDays(1);
        }
        return ranges;
    }


    private record PeriodRange(LocalDate start, LocalDate end) {}


    public IncomeVsExpensesDTO getIncomeVsExpenses(int year) {
        List<Object[]> incomes = transactionRepository.getMonthlyIncome(year, authPersonService.getCurrentPerson().getId());
        List<Object[]> expenses = transactionRepository.getMonthlyExpenses(year, authPersonService.getCurrentPerson().getId());


        Map<String, Double> incomeMap = new HashMap<>();
        for (Object[] row : incomes) {
            incomeMap.put((String) row[0], ((Number) row[1]).doubleValue());
        }

        Map<String, Double> expenseMap = new HashMap<>();
        for (Object[] row : expenses) {
            expenseMap.put((String) row[0], ((Number) row[1]).doubleValue());
        }

        Set<String> allMonths = new TreeSet<>();
        allMonths.addAll(incomeMap.keySet());
        allMonths.addAll(expenseMap.keySet());

        List<IncomeVsExpensesDTO.MonthlyData> monthlyDataList = new ArrayList<>();
        for (String month : allMonths) {
            double income = incomeMap.getOrDefault(month, 0.0);
            double expense = expenseMap.getOrDefault(month, 0.0);
            monthlyDataList.add(new IncomeVsExpensesDTO.MonthlyData(month, income, expense));
        }

        return new IncomeVsExpensesDTO(year, monthlyDataList);
    }

    public MonthStaticDTO getMonthStatic(String YearMonths) {
        YearMonth ym = YearMonth.parse(YearMonths);
        BigDecimal IncomeMonth = transactionRepository.getMonthlyIncome(
                String.valueOf(ym.getYear()), String.format("%02d", ym.getMonthValue()), authPersonService.getCurrentPerson().getId());
        BigDecimal ExpensesMonth = transactionRepository.getMonthlyExpenses(
                String.valueOf(ym.getYear()), String.format("%02d", ym.getMonthValue()), authPersonService.getCurrentPerson().getId());
        if (IncomeMonth == null) IncomeMonth = BigDecimal.ZERO;
        if (ExpensesMonth == null) ExpensesMonth = BigDecimal.ZERO;

        String mostExpensiveCategory = transactionRepository
                .findMostExpensiveCategory(YearMonths, authPersonService.getCurrentPerson().getId())
                .stream()
                .findFirst()
                .orElse(null);


        TransactionSavedDTO largestTx = transactionRepository
                .findLargestTransaction(YearMonths, authPersonService.getCurrentPerson().getId(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(transaction -> modelMapper.map(transaction, TransactionSavedDTO.class))
                .orElse(null);
        MonthStaticDTO dto = new MonthStaticDTO(String.format("%02d", ym.getMonthValue()), IncomeMonth, ExpensesMonth, mostExpensiveCategory, largestTx);
        dto.setMostExpensiveCategory(categoryRepository.findById(largestTx.getCategoryId()).get().getName());

        return dto;

    }


}
