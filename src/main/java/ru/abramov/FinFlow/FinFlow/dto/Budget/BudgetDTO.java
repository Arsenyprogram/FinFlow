package ru.abramov.FinFlow.FinFlow.dto.Budget;


import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BudgetDTO {

    private Long id;

    private Long personId;

    private String category;

    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal currentSpending;

    private BigDecimal remaining;

    private BigDecimal utilizationPercentage;

    private String status;


}
