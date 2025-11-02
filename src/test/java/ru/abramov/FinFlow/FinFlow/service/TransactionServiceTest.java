package ru.abramov.FinFlow.FinFlow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction transaction;
    private Transaction transaction2;
    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(1);
        transaction = new Transaction();
        transaction2 = new Transaction();
        transaction.setId(1L);
        transaction2.setId(2L);
        transaction.setUser(person);
        transaction2.setUser(person);
    }

    @Test
    void findAllByPersonTest() {
        when(transactionRepository.findAllByUserId(person.getId())).thenReturn(Arrays.asList(transaction, transaction2));
        List<Transaction> result = transactionService.findAllByPerson(person);
        assertNotNull(result);
        verify(transactionRepository, times(1)).findAllByUserId(person.getId());
        assertThat(result).hasSize(2);
    }

    @Test
    void findById() {
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        Optional<Transaction> result = transactionService.findById(1L);
        assertNotNull(result);
        verify(transactionRepository, times(1)).findById(transaction.getId());
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    void saveTransaction() {
        transactionService.saveTransaction(transaction);
        assertThat(transaction.getId()).isNull();
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void updateTransaction() {
        transactionService.updateTransaction(transaction);
        verify(transactionRepository, times(1)).save(transaction);
    }

    @Test
    void deleteTransaction() {
        transactionService.deleteTransaction(transaction);
        verify(transactionRepository, times(1)).delete(transaction);

    }
}