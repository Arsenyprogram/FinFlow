package ru.abramov.FinFlow.FinFlow.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PersonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionRepository transactionRepository;

    private Person person;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll(); // сначала чистим транзакции
        personRepository.deleteAll();

        person = new Person();
        person.setPassword(passwordEncoder.encode("12345"));
        person.setName("Test");
        person.setFirstName("Arseny");
        person.setLastName("Abramov");
        person.setPhoneNumber("+79997773331");
        person.setDefaultCurrency("RUB");
        personRepository.save(person);
    }

    @Test
    @WithMockUser(username = "Test")
    @Transactional
    void getCurrentPerson_ShouldReturnInfoAboutPerson() throws Exception {
        mockMvc.perform(get("/users/me")
                        .accept(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Arseny"))
                .andExpect(jsonPath("$.lastName").value("Abramov"))
                .andExpect(jsonPath("$.phoneNumber").value("+79997773331"))
                .andExpect(jsonPath("$.defaultCurrency").value("RUB"));
    }

    @Test
    @WithMockUser(username = "Test")
    @Transactional
    void updatePerson_ShouldUpdateFields() throws Exception {
        String json = """
            {
                "firstName": "Ivan",
                "lastName": "Ivanov",
                "phoneNumber": "+71112223344",
                "defaultCurrency": "USD"
            }
            """;

        mockMvc.perform(patch("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ivan"))
                .andExpect(jsonPath("$.lastName").value("Ivanov"))
                .andExpect(jsonPath("$.phoneNumber").value("+71112223344"))
                .andExpect(jsonPath("$.defaultCurrency").value("USD"));


        Person updated = personRepository.findById((long) person.getId()).orElseThrow();
        assertEquals("Ivan", updated.getFirstName());
        assertEquals("Ivanov", updated.getLastName());
        assertEquals("+71112223344", updated.getPhoneNumber());
        assertEquals("USD", updated.getDefaultCurrency());
    }

    @Test
    @WithMockUser(username = "Test")
    @Transactional
    void deletePerson_WithCorrectPassword_ShouldReturnOk() throws Exception {
        String json = """
                {
                    "password": "12345"
                }
                """;

        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Проверяем, что пользователь помечен как удаленный
        Person deleted = personRepository.findById((long)person.getId()).orElseThrow();
        assertTrue(deleted.getDeleted()); // предполагается, что у тебя есть поле deleted или подобное
    }


    @Test
    @WithMockUser(username = "Test")
    @Transactional

    void deletePerson_WithWrongPassword_ShouldReturnBadRequest() throws Exception {
        String json = """
                {
                    "password": "wrongpass"
                }
                """;

        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Неправильный пароль"));

        // Проверяем, что пользователь не удален
        Person notDeleted = personRepository.findById((long) person.getId()).orElseThrow();
        assertFalse(notDeleted.getDeleted());
    }

}