package ru.abramov.FinFlow.FinFlow.controllers;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final ModelMapper modelMapper;
    private final AuthPersonService authPersonService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<PersonInfoDTO> infoAboutPerson(){
        Person person = authPersonService.getCurrentPerson();
        PersonInfoDTO personInfoDTO = modelMapper.map(person, PersonInfoDTO.class);
        return ResponseEntity.ok(personInfoDTO);
    }

    @PatchMapping("/me")
    public ResponseEntity<PersonInfoDTO> updatePerson(@RequestBody PersonUpdateDTO dto){
        Person person = authPersonService.getCurrentPerson();
        if (dto.getFirstName() != null) person.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) person.setLastName(dto.getLastName());
        if (dto.getPhoneNumber() != null) person.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getDefaultCurrency() != null) person.setDefaultCurrency(dto.getDefaultCurrency());
        personService.update(person);
        PersonInfoDTO personInfoDTO = modelMapper.map(person, PersonInfoDTO.class);
        return ResponseEntity.ok(personInfoDTO);
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
