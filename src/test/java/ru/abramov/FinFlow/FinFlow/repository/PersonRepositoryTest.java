package ru.abramov.FinFlow.FinFlow.repository;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import ru.abramov.FinFlow.FinFlow.entity.Person;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    private Person person;

    @BeforeEach
    void setUp(){
        person = new Person();
        person.setName("Test");
        person.setFirstName("Test");
        person.setLastName("Test");
        person.setPassword("12345");
        person.setPhoneNumber("1234567890");
    }


    @Test
    void findByNameWhenPersonExists() {
        personRepository.save(person);
        Optional<Person> result = personRepository.findByName("Test");
        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void findByNameWhenPersonDoesNotExist() {
        personRepository.save(person);
        Optional<Person> result = personRepository.findByName("NotExists");
        assertTrue(result.isEmpty());
    }

    @Test
    void save_shouldAssignId() {
        personRepository.save(person);
        assertNotNull(person.getId());
    }

    @Test
    void updatePerson(){
        personRepository.save(person);
        person.setName("NewTest");
        personRepository.save(person);
        Optional<Person> result = personRepository.findByName("NewTest");
        assertTrue(result.isPresent());
        assertEquals("NewTest", result.get().getName());
    }

    @Test
    void deletePerson(){
        personRepository.delete(person);
        assertTrue(personRepository.findByName(person.getName()).isEmpty());
    }


}