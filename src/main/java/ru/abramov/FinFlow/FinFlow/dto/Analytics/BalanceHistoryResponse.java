package ru.abramov.FinFlow.FinFlow.dto.Analytics;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BalanceHistoryResponse {

    private BigDecimal startingBalance;
    private List<BalancePointDTO> points;
    private BigDecimal endingBalance;
}
