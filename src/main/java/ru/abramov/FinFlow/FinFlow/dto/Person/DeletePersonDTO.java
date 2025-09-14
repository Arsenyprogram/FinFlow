package ru.abramov.FinFlow.FinFlow.dto.Person;

import lombok.Data;

@Data
public class DeletePersonDTO {
    private String password;
    private String reason;

    public DeletePersonDTO(String password, String reason) {
        this.password = password;
        this.reason = reason;
    }
}
