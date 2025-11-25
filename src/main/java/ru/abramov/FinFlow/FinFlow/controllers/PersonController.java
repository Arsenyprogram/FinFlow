package ru.abramov.FinFlow.FinFlow.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Users",
        description = "Эндпоинты для работы с профилем пользователя"
)
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;
    private final ModelMapper modelMapper;
    private final AuthPersonService authPersonService;
    private final PasswordEncoder passwordEncoder;

    @Operation(
            summary = "Получить информацию о текущем пользователе",
            description = "Возвращает профиль авторизованного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация успешно получена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/me")
    public ResponseEntity<PersonInfoDTO> infoAboutPerson(){
        Person person = authPersonService.getCurrentPerson();
        PersonInfoDTO personInfoDTO = modelMapper.map(person, PersonInfoDTO.class);
        return ResponseEntity.ok(personInfoDTO);
    }


    @Operation(
            summary = "Обновить профиль текущего пользователя",
            description = "Позволяет изменить имя, фамилию, телефон или валюту",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Профиль успешно обновлён"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
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


    @Operation(
            summary = "Удалить аккаунт текущего пользователя",
            description = "Пользователь должен ввести правильный пароль. Удаление мягкое (soft delete).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Аккаунт успешно удалён"),
                    @ApiResponse(responseCode = "400", description = "Неверный пароль"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
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
