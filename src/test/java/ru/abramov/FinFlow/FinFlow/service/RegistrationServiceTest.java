package ru.abramov.FinFlow.FinFlow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private Person person;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        person.setName("Test");
    }

    @Test
    void registrationWhenUserExists() {
        when(personRepository.findByName(person.getName())).thenThrow(RuntimeException.class);
        assertThrows(RuntimeException.class, () -> registrationService.registration(person));
        verify(personRepository, never()).save(any());
    }

    @Test
    void registrationWhenUserDoesNotExist() {

        when(passwordEncoder.encode(person.getPassword())).thenReturn("encodedPassword");
        registrationService.registration(person);
        assertEquals("encodedPassword", person.getPassword());
        verify(personRepository, times(1)).save(person);

    }
}