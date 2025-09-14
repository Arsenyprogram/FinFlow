package ru.abramov.FinFlow.FinFlow.dto.Analytics;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DateSumProjection {
    LocalDate getDate();
    BigDecimal getSum();
}
