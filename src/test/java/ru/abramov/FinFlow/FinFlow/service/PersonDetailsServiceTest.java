package ru.abramov.FinFlow.FinFlow.service;

import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;
import ru.abramov.FinFlow.FinFlow.security.PersonDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonDetailsServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonDetailsService personDetailsService;

    private Person person;

    @BeforeEach
    void setUp(){
        person = new Person();
        person.setName("Test");
        person.setPassword("123");

    }


    @Test
    void loadUserByUsernameWhenUserNotExists() {
        when(personRepository.findByName(person.getName())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> personDetailsService.loadUserByUsername(person.getName()))
        .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
        verify(personRepository, times(1)).findByName(person.getName());
    }

    @Test
    void loadUserByUsernameWhenUserWasDeleted() {
        person.setDeleted(true);
        when(personRepository.findByName(person.getName())).thenReturn(Optional.of(person));
        assertThatThrownBy(() -> personDetailsService.loadUserByUsername(person.getName()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Пользователь был удален");
        verify(personRepository, times(1)).findByName(person.getName());
    }

    @Test
    void loadUserByUsernameWhenUserExists() {
        when(personRepository.findByName(person.getName())).thenReturn(Optional.of(person));
        UserDetails personDetails = personDetailsService.loadUserByUsername(person.getName());
        assertThat(personDetails.getUsername()).isEqualTo(person.getName());
        assertThat(personDetails.getPassword()).isEqualTo(person.getPassword());
        verify(personRepository, times(1)).findByName(person.getName());
    }
}