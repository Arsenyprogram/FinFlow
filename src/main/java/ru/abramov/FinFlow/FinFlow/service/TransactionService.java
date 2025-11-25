package ru.abramov.FinFlow.FinFlow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Cacheable(value ="listTransaction", key="#person.id")
    public List<Transaction> findAllByPerson(Person person) {
        return transactionRepository.findAllByUserId(person.getId());
    }

    @Cacheable(value = "transaction", key="#id", unless = "#result == null")
    public Optional<Transaction> findById(Long id) {
        return transactionRepository.findById(id);
    }

    @Transactional
    @CacheEvict(
            value = {"currentBalance", "balance", "expenseByCategory", "monthStatic", "budget", "listBudgets"},
            key="#transaction.user.id",
            allEntries = true
    )
    public void saveTransaction(Transaction transaction){
        transaction.setId(null);
        transactionRepository.save(transaction);
    }

    @Transactional
    @CacheEvict(
            value = {"currentBalance", "balance", "expenseByCategory", "monthStatic", "budget", "listBudgets"},
            key="#transaction.user.id",
            allEntries = true
    )
    public void updateTransaction(Transaction transaction){
        transactionRepository.save(transaction);
    }

    @Transactional
    @CacheEvict(
            value = {"currentBalance", "balance", "expenseByCategory", "monthStatic", "budget", "listBudgets"},
            key="#transaction.user.id",
            allEntries = true
    )
    public void deleteTransaction(Transaction transaction){
        transactionRepository.delete(transaction);
    }

}
