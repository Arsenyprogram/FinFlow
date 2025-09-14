package ru.abramov.FinFlow.FinFlow.dto.Person;

import jakarta.validation.constraints.NotNull;

public class PersonDTO {
    @NotNull
    private String name;

    @NotNull
    private String defaultCurrency;



}
