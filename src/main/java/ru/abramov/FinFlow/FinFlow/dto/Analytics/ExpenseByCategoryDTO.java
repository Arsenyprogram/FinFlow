package ru.abramov.FinFlow.FinFlow.dto.Analytics;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@NoArgsConstructor
@Data
public class ExpenseByCategoryDTO {
    private String category;
    private BigDecimal total;


    public ExpenseByCategoryDTO(String category, Double total) {
        this.category = category;
        this.total = BigDecimal.valueOf(total);
    }
}