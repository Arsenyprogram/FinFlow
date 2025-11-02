package ru.abramov.FinFlow.FinFlow.dto.Analytics;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
public class IncomeVsExpensesDTO {

    private int year;
    private List<MonthlyData> monthlyData;

    public IncomeVsExpensesDTO(int year, java.util.List<MonthlyData> monthlyData) {
        this.year = year;
        this.monthlyData = monthlyData;
    }
    @Data
    public static class MonthlyData {
        private String month;
        private Double income;
        private Double expenses;
        private Double savings;


        public  MonthlyData(String month, Double income, Double expenses) {
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.savings = income - expenses;
        }

    }
}
