package ru.abramov.FinFlow.FinFlow.dto.Budget;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetSaveDTO {

    private Long categoryId;

    @NotNull(message = "Сумма обязательна")
    private BigDecimal amount;
    private LocalDate startDate;
    private LocalDate endDate;

}
