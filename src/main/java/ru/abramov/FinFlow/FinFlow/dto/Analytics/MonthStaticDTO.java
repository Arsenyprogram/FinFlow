package ru.abramov.FinFlow.FinFlow.dto.Analytics;

import lombok.Data;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionSavedDTO;

import java.math.BigDecimal;


@Data
public class MonthStaticDTO {

    private String month;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal balance;
    private BigDecimal averageDailyExpense;
    private String mostExpensiveCategory;
    private TransactionSavedDTO largestTransaction;



    private int count(String str){
        if(str.endsWith("01") || str.endsWith("03") || str.endsWith("05") || str.endsWith("07") || str.endsWith("08") || str.endsWith("10") || str.endsWith("12")){
            return 31;
        }
        if(str.endsWith("04") || str.endsWith("06") || str.endsWith("09") || str.endsWith("11") ){
            return 30;
        }
        else {
            return 28;
        }
    }

    public MonthStaticDTO(String month, BigDecimal totalIncome, BigDecimal totalExpense, String mostExpensiveCategory, TransactionSavedDTO largestTransaction) {
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.mostExpensiveCategory = mostExpensiveCategory;
        this.largestTransaction = largestTransaction;
        this.balance =  totalIncome.subtract(totalExpense);
        this.averageDailyExpense = totalExpense.divide(BigDecimal.valueOf(count(month)), 2, BigDecimal.ROUND_HALF_UP);



    }
}
