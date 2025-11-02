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
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionDTO;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionSavedDTO;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.AnalyticsService;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.CategoryService;
import ru.abramov.FinFlow.FinFlow.service.TransactionService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthPersonService authPersonService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private ModelMapper modelMapper;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Person person;
    private Transaction transaction;
    private Category category;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        person.setName("Arseny");
        person.setTransactions(new ArrayList<>());
        when(authPersonService.getCurrentPerson()).thenReturn(person);

        category = new Category();
        category.setName("Food");

        transaction = new Transaction();
        transaction.setUser(person);
        transaction.setCategory(category);

    }

    @Test
    @DisplayName("Получение всех транзакций пользователя")
    void getAllTransactions_ShouldReturnList() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setCategory(category);

        when(transactionService.findAllByPerson(person)).thenReturn(List.of(transaction));
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(transactionDTO);

        mockMvc.perform(get("/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(transactionDTO))));
    }

    @Test
    @DisplayName("Получение транзакции по id - успешный кейс")
    void getTransactionById_ShouldReturnTransaction() throws Exception {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setCategory(category);

        when(transactionService.findById(1L)).thenReturn(Optional.of(transaction));
        when(modelMapper.map(transaction, TransactionDTO.class)).thenReturn(transactionDTO);

        mockMvc.perform(get("/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(transactionDTO)));
    }

    @Test
    @DisplayName("Получение транзакции по id - транзакция не найдена")
    void getTransactionById_ShouldReturn404_WhenNotFound() throws Exception {
        when(transactionService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Создание транзакции - успешный кейс")
    void createTransaction_ShouldReturn201_WhenValid() throws Exception {
        TransactionSavedDTO dto = new TransactionSavedDTO();
        dto.setCategoryId(1L);
        dto.setAmount(100.0);
        dto.setType("INCOME");


        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(modelMapper.map(dto, Transaction.class)).thenReturn(transaction);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Создание транзакции - категория не найдена")
    void createTransaction_ShouldReturn400_WhenCategoryNotFound() throws Exception {
        TransactionSavedDTO dto = new TransactionSavedDTO();
        dto.setCategoryId(99L);

        when(categoryService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Такой категории нет"));

    }

    @Test
    @DisplayName("Создание транзакции - ошибки валидации")
    void createTransaction_ShouldReturn400_WhenInvalidData() throws Exception {
        TransactionSavedDTO invalidDTO = new TransactionSavedDTO(); // пустые поля

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("Удаление транзакции - успешный кейс")
    void deleteTransaction_ShouldReturn200_WhenExists() throws Exception {
        when(transactionService.findById(1L)).thenReturn(Optional.of(transaction));

        mockMvc.perform(delete("/transactions/1"))
                .andExpect(status().isOk());
    }
}