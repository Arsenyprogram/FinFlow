package ru.abramov.FinFlow.FinFlow.dto.Person;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "firstName", "lastName", "phoneNumber", "defaultCurrency"})
public class PersonUpdateDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String defaultCurrency;

    public PersonUpdateDTO(String firstName, String lastName, String phoneNumber, String defaultCurrency) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.defaultCurrency = defaultCurrency;
    }

    public PersonUpdateDTO() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
}
