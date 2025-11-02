package ru.abramov.FinFlow.FinFlow.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.abramov.FinFlow.FinFlow.config.JWTFilter;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetSaveDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Budget;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.BudgetService;
import ru.abramov.FinFlow.FinFlow.service.PersonService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;

@WebMvcTest(BudgetController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private AuthPersonService authPersonService;

    @MockitoBean
    private ModelMapper modelMapper;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Budget budget;

    private Person person;


    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        budget = new Budget();
        budget.setPerson(person);
        budget.setAmount(BigDecimal.valueOf(5000));
    }

    @Test
    @DisplayName("Возвращает список бюджетов текущего пользователя")
    void getBudgets() throws Exception {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setAmount(BigDecimal.valueOf(5000));
        budgetDTO.setPersonId((long)person.getId());
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(budgetService.getListBudgets(1)).thenReturn(Collections.singletonList(budgetDTO));
        mockMvc.perform(get("/budgets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(budgetDTO))));
    }

    @Test
    @DisplayName("Возвращает бюджет по id для текущего пользователя")
    void getBudgetById_ShouldReturnBudget() throws Exception {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setAmount(BigDecimal.valueOf(5000));
        budgetDTO.setPersonId(1L);
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(budgetService.getBudgetById(1L, 1)).thenReturn(budgetDTO);

        mockMvc.perform(get("/budgets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.personId").value(1));
    }


    @Test
    @DisplayName("Создаёт новый бюджет — успешный запрос")
    void createBudget_ShouldReturnCreatedBudget() throws Exception {
        BudgetSaveDTO saveDTO = new BudgetSaveDTO();
        saveDTO.setAmount(BigDecimal.valueOf(3000));

        BudgetDTO resultDTO = new BudgetDTO();
        resultDTO.setAmount(BigDecimal.valueOf(3000));

        when(budgetService.save(any(BudgetSaveDTO.class))).thenReturn(resultDTO);

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(saveDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(3000));
    }



    @Test
    @DisplayName("Обновляет существующий бюджет — успешный запрос")
    void updateBudget_ShouldReturnUpdatedBudget() throws Exception {
        BudgetUpdateDTO updateDTO = new BudgetUpdateDTO();
        updateDTO.setAmount(BigDecimal.valueOf(7000));

        BudgetDTO resultDTO = new BudgetDTO();
        resultDTO.setAmount(BigDecimal.valueOf(7000));

        when(budgetService.update(eq(1L), any(BudgetUpdateDTO.class))).thenReturn(resultDTO);

        mockMvc.perform(put("/budgets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(7000));
    }


    @Test
    @DisplayName("Удаляет бюджет по id — успешный запрос")
    void deleteBudget_ShouldReturnNoContent() throws Exception {
        doNothing().when(budgetService).delete(1L);

        mockMvc.perform(delete("/budgets/1"))
                .andExpect(status().isNoContent());
    }


    @Test
    @DisplayName("Возвращает 400 при невалидных данных при создании бюджета")
    void createBudget_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        BudgetSaveDTO invalidDTO = new BudgetSaveDTO();

        mockMvc.perform(post("/budgets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }




}