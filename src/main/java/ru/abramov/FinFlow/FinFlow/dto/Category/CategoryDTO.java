package ru.abramov.FinFlow.FinFlow.dto.Category;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class CategoryDTO {

    private Integer id;

    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    @NotNull
    @Size(min = 2, max = 50)
    private String type;

    private String description;

    private Long personId;

    private Timestamp created_at;

    private Timestamp updated_at;

}
