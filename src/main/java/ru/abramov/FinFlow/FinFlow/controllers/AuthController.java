package ru.abramov.FinFlow.FinFlow.controllers;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonLoginDTO;
import ru.abramov.FinFlow.FinFlow.dto.Person.PersonRegistrationDto;
import ru.abramov.FinFlow.FinFlow.dto.RefreshTokenDTO;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.exception.AccountDeletedException;
import ru.abramov.FinFlow.FinFlow.security.JWTUtil;
import ru.abramov.FinFlow.FinFlow.service.PersonService;
import ru.abramov.FinFlow.FinFlow.service.RegistrationService;
import ru.abramov.FinFlow.FinFlow.util.PersonValidator;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final PersonValidator personValidator;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final PersonService personService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(RegistrationService registrationService, PersonValidator personValidator, JWTUtil jwtUtil, ModelMapper modelMapper, PersonService personService, PasswordEncoder passwordEncoder) {
        this.registrationService = registrationService;
        this.personValidator = personValidator;
        this.jwtUtil = jwtUtil;
        this.modelMapper = modelMapper;
        this.personService = personService;
        this.passwordEncoder = passwordEncoder;
    }


    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody @Valid PersonRegistrationDto personRegistrationDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(ObjectError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }

        try {
            Person person = modelMapper.map(personRegistrationDto, Person.class);
            personValidator.validate(person, bindingResult);
            if(bindingResult.hasErrors()) {
                return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
            }
            registrationService.registration(person);
            String token = jwtUtil.generateAccessToken(personRegistrationDto.getName());
            String refreshToken = jwtUtil.generateRefreshToken(personRegistrationDto.getName());
            return ResponseEntity.ok(Map.of("token", token, "refreshToken", refreshToken));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid PersonLoginDTO personLoginDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for(ObjectError objectError : bindingResult.getAllErrors()) {
                errors.add(objectError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        try {
            Person person = modelMapper.map(personLoginDTO, Person.class);
            Optional<Person> optionalPerson = personService.findByName(person.getName());
            if(optionalPerson.isPresent()) {
                Person personFound = optionalPerson.get();
                if(personFound.getDeleted()){
                    if(personFound.getRestoreDeadline().before(Timestamp.valueOf(LocalDateTime.now()))) {
                        throw new AccountDeletedException("срок восстановления аккаунта истек");
                    }
                }
                if (passwordEncoder.matches( person.getPassword(), personFound.getPassword())){
                    return ResponseEntity.ok(Map.of("accessToken", jwtUtil.generateAccessToken(personFound.getName()),
                            "refreshToken", jwtUtil.generateRefreshToken(personFound.getName())));
                }
                else {
                    return ResponseEntity.badRequest().body(Map.of("errors", "Неправильный пароль"));
                }
            }
            else {
                return ResponseEntity.badRequest().body(Map.of("errors", "Неправильный логин"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("errors", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshTokenDTO refreshTokenDTO) {
        try {
            String name = jwtUtil.verifyRefreshToken(refreshTokenDTO.getRefreshToken());
            String newAccessToken = jwtUtil.generateAccessToken(name);
            Map<String, String> map = new HashMap<>();
            map.put("accessToken", newAccessToken);
            map.put("refreshToken", refreshTokenDTO.getRefreshToken());
            return ResponseEntity.ok(map);
        } catch (JWTVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Successfully logged out"));
    }

}
