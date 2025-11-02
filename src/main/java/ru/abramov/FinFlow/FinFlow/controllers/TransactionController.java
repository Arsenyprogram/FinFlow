package ru.abramov.FinFlow.FinFlow.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionDTO;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionSavedDTO;
import ru.abramov.FinFlow.FinFlow.dto.Transactional.TransactionUpdate;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.entity.Transaction;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.CategoryService;
import ru.abramov.FinFlow.FinFlow.service.TransactionService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ModelMapper modelMapper;
    private final AuthPersonService authPersonService;
    private final CategoryService categoryService;


    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        Person person = authPersonService.getCurrentPerson();
        List<TransactionDTO> transactionDTOList = transactionService.findAllByPerson(person).stream()
                .map(dto -> modelMapper.map(dto, TransactionDTO.class))
                .toList();
        return ResponseEntity.ok(transactionDTOList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Транзакция с id=" + id + " не найдена"));
        return ResponseEntity.ok(modelMapper.map(transaction, TransactionDTO.class));
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody @Valid TransactionSavedDTO transactionSavedDTO, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            List<String> errors = new ArrayList<>();
            for (ObjectError objectError : bindingResult.getAllErrors()) {
                errors.add(objectError.getDefaultMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        Person person = authPersonService.getCurrentPerson();
        Optional<Category> category = categoryService.findById(transactionSavedDTO.getCategoryId());
        if(category.isEmpty()){
            return ResponseEntity.badRequest().body("Такой категории нет");
        }
        Transaction transaction = modelMapper.map(transactionSavedDTO, Transaction.class);
        transaction.setCategory(category.get());
        transaction.setUser(person);
        transaction.setCreatedAt(Timestamp.from(Instant.now()));
        person.getTransactions().add(transaction);
        transactionService.saveTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody @Valid TransactionUpdate transactionUpdate, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors()
                    .stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            return ResponseEntity.badRequest().body(Map.of("errors", errors));
        }
        Transaction transaction = transactionService.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Транзакция с id=" + id + " не найдена"));
        Category category = categoryService.findById(transactionUpdate.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Категория с id=" + transactionUpdate.getCategoryId() + " не найдена"));
        transaction.setAmount(transactionUpdate.getAmount());
        transaction.setType(transactionUpdate.getType());
        transaction.setDate(transactionUpdate.getDate());
        transaction.setCategory(category);
        transaction.setUpdatedAt(Timestamp.from(Instant.now()));
        transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable Long id) {
        Transaction transaction = transactionService.findById(id).orElseThrow(() -> new NoSuchElementException("Транзакция с id=" + id + " не найдена"));
        transactionService.deleteTransaction(transaction);
        return ResponseEntity.ok(HttpStatus.OK);
    }

}
