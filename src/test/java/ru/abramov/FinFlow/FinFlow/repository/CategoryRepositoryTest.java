package ru.abramov.FinFlow.FinFlow.repository;

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
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private BudgetRepositories budgetRepositories;

    private Person person;
    private Category category;

    @BeforeEach
    void setUp() {

        person = new Person();
        person.setName("TestUser");
        person.setPassword("12345");
        person.setPhoneNumber("1234567890");
        person.setFirstName("TestUser");
        person.setLastName("TestUser");
        personRepository.save(person);

        category = new Category();
        category.setName("Food");
        category.setType("EXPENSE");
        category.setUser(person);
        categoryRepository.save(category);
    }

    @Test
    @DisplayName("Категория сохраняется с пользователем")
    void categoryShouldBeSavedWithUser() {
        Optional<Category> found = categoryRepository.findById(category.getId());
        assertTrue(found.isPresent());
        assertEquals("Food", found.get().getName());
        assertNotNull(found.get().getUser());
        assertEquals(person.getId(), found.get().getUser().getId());
    }

    @Test
    @DisplayName("Категория связана с бюджетом и корректно подгружается через BudgetRepository")
    void categoryShouldBeFetchedViaBudget() {
        Budget budget = new Budget();
        budget.setPerson(person);
        budget.setCategory(category);
        budget.setAmount(BigDecimal.valueOf(500));
        budget.setStartDate(LocalDate.now());
        budget.setEndDate(LocalDate.now().plusDays(30));
        budgetRepositories.save(budget);


        List<Budget> budgets = budgetRepositories.findAllByPersonId(person.getId());
        assertEquals(1, budgets.size());
        Budget foundBudget = budgets.get(0);
        assertNotNull(foundBudget.getCategory(), "Category should be fetched via JOIN FETCH");
        assertEquals(category.getId(), foundBudget.getCategory().getId());
    }

    @Test
    @DisplayName("Категория не найдена для несуществующего ID")
    void categoryNotFoundForInvalidId() {
        Optional<Category> found = categoryRepository.findById(999);
        assertTrue(found.isEmpty());
    }
}
