package ru.abramov.FinFlow.FinFlow.util;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.PersonRepository;

@Component
public class PersonValidator implements Validator {

    private final PersonRepository personRepository;


    public PersonValidator(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(Person.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;
        if(personRepository.findByName(person.getName()).isEmpty()){
            return;
        }
        else{
            errors.rejectValue("name", "", "Человек с таким именем уже существует");
        }
    }
}
