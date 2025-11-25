package ru.abramov.FinFlow.FinFlow.exception;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
@AutoConfigureMockMvc(addFilters = false)
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Runtime error");
        ResponseEntity<?> response = handler.handRuntime(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Runtime error", ((Map<?, ?>)response.getBody()).get("message"));
    }

    @Test
    void testHandleAuthException() {
        AuthException ex = new AuthException("Unauthorized");
        ResponseEntity<?> response = handler.handleAuthException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized", ((Map<?, ?>)response.getBody()).get("error"));
    }

    @Test
    void testHandleNotFoundException() {
        NoSuchElementException ex = new NoSuchElementException("Not found");
        ResponseEntity<?> response = handler.handleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", ((Map<?, ?>)response.getBody()).get("error"));
    }

    @Test
    void testHandleExpiredToken() {
        ExpiredTokenException ex = new ExpiredTokenException("Token expired");
        ResponseEntity<?> response = handler.handleExpiredToken(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token expired", ((Map<?, ?>)response.getBody()).get("error"));
    }

    @Test
    void testHandleAccountDeletedException() {
        AccountDeletedException ex = new AccountDeletedException("Account deleted");
        ResponseEntity<?> response = handler.handleAccountDeletedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account deleted", ((Map<?, ?>)response.getBody()).get("error"));
    }

    @Test
    void testHandleDataIntegrityViolationUniqueBudget() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "unique_budget_per_user_category_period constraint violated");

        ResponseEntity<String> response = handler.handleDataIntegrityViolation(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Бюджет для этой категории на этот период уже существует", response.getBody());
    }

    @Test
    void testHandleDataIntegrityViolationOther() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "some other DB error");
        ResponseEntity<String> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Ошибка базы данных"));
    }

}