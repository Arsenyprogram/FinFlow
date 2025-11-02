package ru.abramov.FinFlow.FinFlow.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import ru.abramov.FinFlow.FinFlow.dto.Category.CategoryDTO;
import ru.abramov.FinFlow.FinFlow.dto.Category.CategorySaveDTO;
import ru.abramov.FinFlow.FinFlow.dto.Category.CategoryUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.CategoryService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private  CategoryService categoryService;

    @MockitoBean
    private  ModelMapper modelMapper;

    @MockitoBean
    private  AuthPersonService authPersonService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Category category;
    private Person person;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Food");
        category.setDescription("Good food");
        category.setType("INCOME");

        person = new Person();
        person.setId(1);
        person.setCategories(new ArrayList<>(List.of(category)));
    }

    @Test
    void findAll() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Food");
        categoryDTO.setDescription("Good food");
        categoryDTO.setType("INCOME");
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);
        when(categoryService.findAllForPersonAndSystem(person)).thenReturn(List.of(category));
        mockMvc.perform(get("/categories")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(categoryDTO))));
    }

    @Test
    void findById() throws Exception{
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Food");
        categoryDTO.setDescription("Good food");
        categoryDTO.setType("INCOME");
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);
        when(categoryService.findById(1l)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/categories/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().json(new ObjectMapper().writeValueAsString(categoryDTO)));


    }

    @Test
    void save() throws Exception {
        CategorySaveDTO categorySaveDTO = new CategorySaveDTO();
        categorySaveDTO.setName("Food");
        categorySaveDTO.setDescription("Good food");
        categorySaveDTO.setType("INCOME");
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(modelMapper.map(categorySaveDTO, Category.class)).thenReturn(category);
        doNothing().when(categoryService).save(any(Category.class));
        mockMvc.perform(post("/categories")
            .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(categorySaveDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void save_ShouldReturn400_WhenValidationFails() throws Exception {
        CategorySaveDTO invalidDTO = new CategorySaveDTO();
        invalidDTO.setName("");  // невалидно
        invalidDTO.setDescription("Good food");
        invalidDTO.setType("");  // невалидно

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].defaultMessage").exists());
    }


    @Test
    void update_ShouldReturnOk_WhenValidData() throws Exception {
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setName("Updated Food");
        dto.setDescription("Very good food");

        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryService).save(any(Category.class));

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        CategoryUpdateDTO dto = new CategoryUpdateDTO();
        dto.setName("");

        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void delete_ShouldReturnOk_WhenCategoryExists() throws Exception {
        when(categoryService.findById(1L)).thenReturn(Optional.of(category));
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        doNothing().when(categoryService).delete(category);

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_ShouldReturnError_WhenCategoryNotFound() throws Exception {
        when(categoryService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNotFound());

    }


    @Test
    void findDefault_ShouldReturnListOfCategories() throws Exception {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setName("Food");
        categoryDTO.setDescription("Good food");
        categoryDTO.setType("INCOME");

        when(categoryService.findAllForSystem()).thenReturn(List.of(category));
        when(modelMapper.map(category, CategoryDTO.class)).thenReturn(categoryDTO);

        mockMvc.perform(get("/categories/default"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(List.of(categoryDTO))));
    }


}