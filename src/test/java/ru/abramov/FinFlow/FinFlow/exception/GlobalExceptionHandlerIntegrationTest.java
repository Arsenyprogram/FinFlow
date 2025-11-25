package ru.abramov.FinFlow.FinFlow.exception;

import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.config.JWTFilter;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.service.BudgetService;
import ru.abramov.FinFlow.FinFlow.service.PersonService;
import ru.abramov.FinFlow.FinFlow.service.RegistrationService;

import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;
    @MockitoBean
    private JWTFilter jwtFilter;

    @Autowired
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        Person person = new Person();
        person.setName("Test2");
        person.setPassword("12345");
        person.setFirstName("Test");
        person.setLastName("Test");
        person.setPhoneNumber("1234567890");
        registrationService.registration(person);

    }


    @Test
    @WithMockUser(username = "Test2")
    void testNotFoundExceptionHandled() throws Exception {
        when(budgetService.getBudgetById(anyLong(), anyInt()))
                .thenThrow(new NoSuchElementException("Budget not found"));

        mockMvc.perform(get("/budgets/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Budget not found"));
    }

    @Test
    void testUnauthenticatedUserReturns401() throws Exception {
        mockMvc.perform(get("/budgets"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("User is not authenticated"));
    }
}
