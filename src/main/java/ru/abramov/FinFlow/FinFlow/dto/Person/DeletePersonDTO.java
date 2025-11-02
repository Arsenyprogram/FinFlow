package ru.abramov.FinFlow.FinFlow.dto.Person;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeletePersonDTO {
    private String password;
    private String reason;

}
