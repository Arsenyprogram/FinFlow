package ru.abramov.FinFlow.FinFlow.dto.Budget;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetUpdateDTO {
    private BigDecimal amount;
    private LocalDate endDate;
}
