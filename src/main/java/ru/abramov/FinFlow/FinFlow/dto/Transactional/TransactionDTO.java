package ru.abramov.FinFlow.FinFlow.dto.Transactional;

import lombok.Data;
import ru.abramov.FinFlow.FinFlow.entity.Category;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
public class TransactionDTO {

    private Long id;

    private Double amount;

    private String type;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private LocalDate date;

    private Category category;

    public TransactionDTO() {
    }

    public TransactionDTO(Long id, Double amount, String type, Timestamp createdAt, Category category) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
        this.category = category;
    }
}
