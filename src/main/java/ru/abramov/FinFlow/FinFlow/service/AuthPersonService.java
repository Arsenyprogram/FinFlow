package ru.abramov.FinFlow.FinFlow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.exception.AuthException;

@Service
@RequiredArgsConstructor
public class AuthPersonService {

    private final PersonService personService;

    public Person getCurrentPerson() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthException("User is not authenticated");
        }

        return personService.findByName(auth.getName())
                .orElseThrow(() -> new AuthException("User not found"));
    }
}
