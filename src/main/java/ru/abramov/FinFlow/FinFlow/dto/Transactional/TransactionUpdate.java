package ru.abramov.FinFlow.FinFlow.dto.Transactional;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionUpdate {

    private Double amount;

    private String type;

    private Long categoryId;

    private LocalDate date;


}
