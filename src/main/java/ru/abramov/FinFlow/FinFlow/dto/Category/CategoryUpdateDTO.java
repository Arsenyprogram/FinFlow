package ru.abramov.FinFlow.FinFlow.dto.Category;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateDTO {

    @NotNull
    @Size(min = 2, max = 50)
    private String name;

    @NotNull
    private String description;

}
