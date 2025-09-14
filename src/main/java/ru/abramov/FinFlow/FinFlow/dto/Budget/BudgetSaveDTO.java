package ru.abramov.FinFlow.FinFlow.dto.Budget;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetSaveDTO {

    private Long categoryId;
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;

}
