package ru.abramov.FinFlow.FinFlow.dto.Analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BalancePointDTO {

    private String label;
    private LocalDate start;
    private LocalDate end;
    private BigDecimal change;
    private BigDecimal balance;


}
