package ru.abramov.FinFlow.FinFlow.dto.Person;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonRegistrationDto {
    @NotNull
    @Size(min = 2, max = 50)
    private String name;
    @NotNull
    @Size(min = 2, max = 50)
    private String password;

    private String defaultCurrency;

    private String firstName;
    private String lastName;
    private String phoneNumber;


    public PersonRegistrationDto(String name, String password, String defaultCurrency) {
        this.name = name;
        this.password = password;
        this.defaultCurrency = defaultCurrency;
    }
}
