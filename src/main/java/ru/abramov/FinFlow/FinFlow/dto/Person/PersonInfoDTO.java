package ru.abramov.FinFlow.FinFlow.dto.Person;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "name", "firstName", "lastName", "phoneNumber", "defaultCurrency", "created_at"})
public class PersonInfoDTO {
    private int id;
    private String name;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String defaultCurrency;
    private String created_at;


}
