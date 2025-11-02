package ru.abramov.FinFlow.FinFlow.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.exception.AuthException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthPersonServiceTest {

    @Mock
    private PersonService personService;

    @InjectMocks
    private AuthPersonService authPersonService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentPerson_UserAuthenticated_UserFound() {
        Person person = new Person();
        person.setName("John");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("John");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(personService.findByName("John")).thenReturn(Optional.of(person));

        Person result = authPersonService.getCurrentPerson();
        assertEquals(person, result);
    }

    @Test
    void getCurrentPerson_UserNotAuthenticated_ThrowsException() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        AuthException exception = assertThrows(AuthException.class,
                () -> authPersonService.getCurrentPerson());
        assertEquals("User is not authenticated", exception.getMessage());
    }

    @Test
    void getCurrentPerson_UserNotFound_ThrowsException() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("John");

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        when(personService.findByName("John")).thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class,
                () -> authPersonService.getCurrentPerson());
        assertEquals("User not found", exception.getMessage());
    }
}