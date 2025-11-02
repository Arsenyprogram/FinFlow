package ru.abramov.FinFlow.FinFlow.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.MediaType;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;


import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.abramov.FinFlow.FinFlow.config.JWTFilter;

import ru.abramov.FinFlow.FinFlow.dto.Person.DeletePersonDTO;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonInfoDTO;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.PersonService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = PersonController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        person.setName("Test");
        person.setFirstName("Arseny");
        person.setLastName("Abramov");
        person.setPhoneNumber("+79992223331");
        person.setDefaultCurrency("USD");
        person.setPassword("12345");

        TypeMap<DeletePersonDTO, Person> typeMap = (TypeMap<DeletePersonDTO, Person>) mock(TypeMap.class);

        when(modelMapper.typeMap(DeletePersonDTO.class, Person.class)).thenReturn(typeMap);
        when(typeMap.addMappings(any(PropertyMap.class))).thenReturn(typeMap);
    }

    @Test
    @DisplayName("Получение данных пользователя — успешный запрос")
    void infoAboutPerson() throws Exception {
        PersonInfoDTO personInfoDTO = new PersonInfoDTO(1, "Test", "Arseny", "Abramov", "+79992223331", "USD", "2025.01.10");
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(modelMapper.map(person, PersonInfoDTO.class)).thenReturn(personInfoDTO);

        mockMvc.perform(get("/users/me")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(personInfoDTO)));
    }


    @Test
    @DisplayName("Обновление данных пользователя — успешный запрос")
    void updatePerson() throws Exception {
        PersonUpdateDTO personUpdateDTO = new PersonUpdateDTO("Arseny", "Abramov", "+79997773331", "RUB");
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(modelMapper.map(person, PersonInfoDTO.class)).thenReturn( new PersonInfoDTO(1, "Test", "Arseny", "Abramov", "+79997773331", "RUB", "2025.01.10"));
        mockMvc.perform(patch("/users/me")
                .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                .content(new ObjectMapper().writeValueAsString(personUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(personUpdateDTO)));
    }

    @Test
    void deletePerson_ShouldReturnBadRequest_WhenPasswordIncorrect() throws Exception {
        // given
        DeletePersonDTO dto = new DeletePersonDTO();
        dto.setPassword("wrongPassword");

        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(passwordEncoder.matches(dto.getPassword(), person.getPassword())).thenReturn(false);

        // when + then
        mockMvc.perform(delete("/users/me")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                            {"password": "wrongPassword"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Неправильный пароль"));

        verify(personService, never()).softDelete(any());
    }

    @Test
    void deletePerson_ShouldReturnOk_WhenPasswordCorrect() throws Exception {
        // given
        DeletePersonDTO dto = new DeletePersonDTO();
        dto.setPassword("correctPassword");

        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(passwordEncoder.matches(dto.getPassword(), person.getPassword())).thenReturn(true);

        // when + then
        mockMvc.perform(delete("/users/me")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON))
                        .content("""
                            {"password": "correctPassword"}
                        """))
                .andExpect(status().isOk());

        verify(personService, times(1)).softDelete(person);
    }


}