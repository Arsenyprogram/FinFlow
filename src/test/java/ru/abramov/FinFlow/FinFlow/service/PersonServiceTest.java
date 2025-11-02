package ru.abramov.FinFlow.FinFlow.service;

import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person person;

    @BeforeEach
    void setUp(){
        person = new Person();
        person.setId(1);
        person.setName("Test");
    }

    @Test
    void findByNameWhenNameExists() {
        when(personRepository.findByName("Test")).thenReturn(Optional.of(person));

        Optional<Person> result = personService.findByName("Test");
        assertThat(result)
                .isPresent()
                .contains(person);

        verify(personRepository, times(1)).findByName("Test");
    }

    @Test
    void findByNameWhenNameNotExists() {
        when(personRepository.findByName("Test")).thenReturn(Optional.empty());

        Optional<Person> result = personService.findByName("Test");
        assertThat(result).isEmpty();
        verify(personRepository, times(1)).findByName("Test");
    }

    @Test
    void update() {
        when(personRepository.save(person)).thenReturn(person);
        personService.update(person);
        verify(personRepository, times(1)).save(person);
    }

    @Test
    void softDelete() {

        when(personRepository.save(person)).thenReturn(person);

        personService.softDelete(person);
        verify(personRepository, times(1)).save(person);
        assertThat(person.getDeleted()).isTrue();
        assertThat(person.getDeletedAt()).isNotNull();
        assertThat(person.getRestoreDeadline()).isNotNull();
    }
}