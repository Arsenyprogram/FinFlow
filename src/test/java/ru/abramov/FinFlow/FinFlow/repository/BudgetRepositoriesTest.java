package ru.abramov.FinFlow.FinFlow.repository;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.abramov.FinFlow.FinFlow.entity.Budget;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class BudgetRepositoriesTest {

    @Autowired
    private BudgetRepositories budgetRepositories;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Budget budget;
    private Category category;
    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setName("Test");
        person.setFirstName("Test");
        person.setLastName("Test");
        person.setPassword("12345");
        person.setPhoneNumber("1234567890");
        personRepository.save(person);

        category = new Category();
        category.setName("Food");
        category.setType("EXPENSE");
        category.setUser(person);
        categoryRepository.save(category);

        budget = new Budget();
        budget.setPerson(person);
        budget.setCategory(category);
        budget.setAmount(BigDecimal.valueOf(100));
        budget.setStartDate(LocalDate.now());
        budget.setEndDate(LocalDate.now().plusDays(30));
        budgetRepositories.save(budget);

    }

    @Test
    void findAllByPersonId() {
        Budget budget2 = new Budget();
        budget2.setPerson(person);
        budget2.setCategory(category);
        budget2.setAmount(BigDecimal.valueOf(1001));
        budget2.setStartDate(LocalDate.now().plusDays(30));
        budget2.setEndDate(LocalDate.now().plusDays(60));
        budgetRepositories.save(budget2);

        List<Budget> result = budgetRepositories.findAllByPersonId(person.getId());
        assertEquals(2, result.size());
    }

    @Test
    void findByPersonIdAndId() {
        Optional<Budget> result = budgetRepositories.findByPersonIdAndId(person.getId(), budget.getId());
        assertTrue(result.isPresent());
        assertEquals(budget.getId(), result.get().getId());
        assertEquals(category.getId(), result.get().getCategory().getId());
    }

    @Test
    @DisplayName("findByPersonIdAndId — возвращает пусто, если бюджета с таким ID не существует")
    void findByPersonIdAndId_shouldReturnEmptyForNonexistentBudget() {
        Optional<Budget> result = budgetRepositories.findByPersonIdAndId(person.getId(), 999L);
        assertTrue(result.isEmpty());
    }


    @Test
    @DisplayName("findAllByPersonId — возвращает пустой список, если у пользователя нет бюджетов")
    void findAllByPersonId_shouldReturnEmptyForAnotherPerson() {
        Person other = new Person();
        other.setName("AnotherUser");
        other.setPassword("password");
        other.setPhoneNumber("1234567890");
        other.setFirstName("AnotherUser");
        other.setLastName("AnotherUser");
        personRepository.save(other);

        List<Budget> result = budgetRepositories.findAllByPersonId(other.getId());

        assertTrue(result.isEmpty());
    }

}