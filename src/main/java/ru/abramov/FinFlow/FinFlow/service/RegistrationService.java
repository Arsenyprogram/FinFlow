package ru.abramov.FinFlow.FinFlow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;

@Service
@Transactional(readOnly = true)
public class RegistrationService {

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public RegistrationService(PersonRepository personRepository, PasswordEncoder passwordEncoder) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registration(Person person){
        if(personRepository.findByName(person.getName()).isPresent()){
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        personRepository.save(person);
    }

}
