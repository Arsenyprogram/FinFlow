package ru.abramov.FinFlow.FinFlow.dto.Transactional;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.abramov.FinFlow.FinFlow.entity.Category;

import java.time.LocalDate;

@Data
public class TransactionSavedDTO {

    private Double amount;

    private String type;

    private LocalDate date;

    @NotNull(message = "Category is required")
    private Long categoryId;
}
