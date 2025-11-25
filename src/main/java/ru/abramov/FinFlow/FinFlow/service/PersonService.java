package ru.abramov.FinFlow.FinFlow.service;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;


    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }
    
    public Optional<Person> findByName(String name) {
        return personRepository.findByName(name);
    }

    @Transactional
    public void update(Person person) {
        personRepository.save(person);
    }


    @Transactional
    public void softDelete(Person person){
        person.setDeleted(true);
        person.setDeletedAt(Timestamp.from(Instant.now()));
        person.setRestoreDeadline(Timestamp.from(Instant.now().plusSeconds(2592000)));
        personRepository.save(person);
    }

}
