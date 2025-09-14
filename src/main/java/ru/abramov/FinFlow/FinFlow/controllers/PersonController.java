package ru.abramov.FinFlow.FinFlow.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import ru.abramov.FinFlow.FinFlow.dto.Person.DeletePersonDTO;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonInfoDTO;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.PersonService;

@RestController
@RequestMapping("/users")
public class PersonController {

    private final PersonService personService;
    private final ModelMapper modelMapper;
    private final AuthPersonService authPersonService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PersonController(PersonService personService, ModelMapper modelMapper, AuthPersonService authPersonService, PasswordEncoder passwordEncoder) {
        this.personService = personService;
        this.modelMapper = modelMapper;
        this.authPersonService = authPersonService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public ResponseEntity<?> infoAboutPerson(){
        Person person = authPersonService.getCurrentPerson();
        PersonInfoDTO personInfoDTO = modelMapper.map(person, PersonInfoDTO.class);
        return  new ResponseEntity<>(personInfoDTO, HttpStatus.OK);
    }

    @PutMapping("/me")
    public ResponseEntity<?> updatePerson(@RequestBody PersonUpdateDTO dto){
        Person person = authPersonService.getCurrentPerson();
        if (dto.getFirstName() != null) person.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) person.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) person.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDefaultCurrency() != null) person.setDefaultCurrency(dto.getDefaultCurrency());
        personService.update(person);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> deletePerson(@RequestBody DeletePersonDTO dto){
        modelMapper.typeMap(DeletePersonDTO.class, Person.class)
                .addMappings(mapper -> mapper.skip(Person::setPassword));
        Person person = authPersonService.getCurrentPerson();
        if(!passwordEncoder.matches(dto.getPassword(), person.getPassword())){
           return ResponseEntity.badRequest().body(java.util.Map.of("errors", "Неправильный пароль"));
        }
        modelMapper.map(dto, person);
        personService.softDelete(person);
        return new ResponseEntity<>(HttpStatus.OK);
    }




}
