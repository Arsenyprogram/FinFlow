package ru.abramov.FinFlow.FinFlow.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
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
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonLoginDTO;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonRegistrationDto;
import ru.abramov.FinFlow.FinFlow.dto.RefreshTokenDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.PersonService;
import ru.abramov.FinFlow.FinFlow.service.RegistrationService;
import ru.abramov.FinFlow.FinFlow.util.PersonValidator;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private PersonValidator personValidator;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private JWTFilter jwtFilter;

    @MockitoBean
    private ModelMapper modelMapper;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;


    private Person person;

    @BeforeEach
    void setUp() {

        person = new Person();
        person.setId(1);
        person.setName("Arseny");
        person.setPassword("encoded_pass");
        person.setDeleted(false);
    }


    @Test
    @DisplayName("Успешная регистрация пользователя возвращает токены")
    void registration_ShouldReturnTokens_WhenValidData() throws Exception {
        PersonRegistrationDto dto = new PersonRegistrationDto("Arseny", "12345", "RUB");

        when(modelMapper.map(any(PersonRegistrationDto.class), eq(Person.class))).thenReturn(person);
        doNothing().when(personValidator).validate(any(), any());
        doNothing().when(registrationService).registration(any());
        when(jwtUtil.generateAccessToken("Arseny")).thenReturn("access123");
        when(jwtUtil.generateRefreshToken("Arseny")).thenReturn("refresh123");

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"));
    }

    @Test
    @DisplayName("Регистрация возвращает 400 при невалидных данных")
    void registration_ShouldReturn400_WhenInvalidData() throws Exception {
        PersonRegistrationDto invalidDto = new PersonRegistrationDto(); // поля пустые

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Регистрация возвращает 400 при исключении сервиса")
    void registration_ShouldReturn400_WhenRuntimeException() throws Exception {
        PersonRegistrationDto dto = new PersonRegistrationDto("Arseny", "12345", "RUB");

        when(modelMapper.map(any(PersonRegistrationDto.class), eq(Person.class))).thenReturn(person);
        doThrow(new RuntimeException("Ошибка регистрации")).when(registrationService).registration(any());

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Ошибка регистрации"));
    }


    @Test
    @DisplayName("Успешный логин возвращает accessToken и refreshToken")
    void login_ShouldReturnTokens_WhenValidCredentials() throws Exception {
        // DTO с "чистым" паролем от клиента
        PersonLoginDTO dto = new PersonLoginDTO("Arseny", "12345");

        Person personFromDto = new Person();
        personFromDto.setName(dto.getName());
        personFromDto.setPassword(dto.getPassword()); // "12345"
        when(modelMapper.map(any(PersonLoginDTO.class), eq(Person.class))).thenReturn(personFromDto);

        Person personFromDb = new Person();
        personFromDb.setName("Arseny");
        personFromDb.setPassword("encoded_pass"); // пароль в БД
        when(personService.findByName("Arseny")).thenReturn(Optional.of(personFromDb));

        when(passwordEncoder.matches("12345", "encoded_pass")).thenReturn(true);

        when(jwtUtil.generateAccessToken("Arseny")).thenReturn("access123");
        when(jwtUtil.generateRefreshToken("Arseny")).thenReturn("refresh123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access123"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"));
    }

    @Test
    @DisplayName("Логин возвращает 400 при неправильном пароле")
    void login_ShouldReturn400_WhenInvalidPassword() throws Exception {
        PersonLoginDTO dto = new PersonLoginDTO("Arseny", "wrongpass");

        when(modelMapper.map(any(PersonLoginDTO.class), eq(Person.class))).thenReturn(person);
        when(personService.findByName("Arseny")).thenReturn(Optional.of(person));
        when(passwordEncoder.matches("wrongpass", "encoded_pass")).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Неправильный пароль"));
    }

    @Test
    @DisplayName("Логин возвращает 400 при неправильном логине")
    void login_ShouldReturn400_WhenUserNotFound() throws Exception {
        PersonLoginDTO dto = new PersonLoginDTO("Unknown", "12345");

        when(modelMapper.map(any(PersonLoginDTO.class), eq(Person.class))).thenReturn(person);
        when(personService.findByName("Unknown")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Неправильный логин"));
    }

    @Test
    @DisplayName("Обновление токена — успешный сценарий")
    void refresh_ShouldReturnNewAccessToken() throws Exception {
        RefreshTokenDTO refreshTokenDTO = new RefreshTokenDTO("refresh123");

        when(jwtUtil.verifyRefreshToken("refresh123")).thenReturn("Arseny");
        when(jwtUtil.generateAccessToken("Arseny")).thenReturn("newAccessToken");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(refreshTokenDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("refresh123"));
    }

    @Test
    @DisplayName("Refresh возвращает 401 при недействительном токене")
    void refresh_ShouldReturn401_WhenInvalidToken() throws Exception {
        RefreshTokenDTO dto = new RefreshTokenDTO("badToken");

        when(jwtUtil.verifyRefreshToken("badToken")).thenThrow(new JWTVerificationException("expired"));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired refresh token"));
    }

    @Test
    @DisplayName("Logout возвращает сообщение об успешном выходе")
    void logout_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully logged out"));
    }
}
