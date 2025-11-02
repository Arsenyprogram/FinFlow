package ru.abramov.FinFlow.FinFlow.dto.Person;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({ "firstName", "lastName", "phoneNumber", "defaultCurrency"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonUpdateDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String defaultCurrency;

}
