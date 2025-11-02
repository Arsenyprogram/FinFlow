package ru.abramov.FinFlow.FinFlow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetSaveDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Budget;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.BudgetRepositories;
import ru.abramov.FinFlow.FinFlow.repository.CategoryRepository;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepositories budgetRepositories;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthPersonService authPersonService;

    @Mock
    private CategoryRepository categoryRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();


    @InjectMocks
    private BudgetService budgetService;

    private Budget budget;
    private Person person;
    private Category category;

    @BeforeEach
    void setUp(){
        person = new Person();
        person.setId(1);

        category = new Category();
        category.setId(1);
        category.setName("Food");

        budget = new Budget();
        budget.setId(1L);
        budget.setPerson(person);
        budget.setCategory(category);
        budget.setAmount(new BigDecimal("1000"));
        budget.setStartDate(LocalDate.now());
        budget.setEndDate(LocalDate.now().plusDays(30));

    }



    @Test
    void getListBudgets() {

        Person person2 = new Person();
        Budget budget2 = new Budget();
        budget2.setId(1L);
        budget2.setPerson(person2);
        budget2.setCategory(category);
        budget2.setAmount(new BigDecimal("1000"));
        budget2.setStartDate(LocalDate.now());
        budget2.setEndDate(LocalDate.now().plusDays(30));

        when(budgetRepositories.findAllByPersonId(person.getId())).thenReturn(Arrays.asList(budget));

        when(transactionRepository.getTotalExpensesByCategoryAndPeriod(
                person.getId(), category.getId(), budget.getStartDate(), budget.getEndDate()))
                .thenReturn(new BigDecimal("200"));

        when(modelMapper.map(budget, BudgetDTO.class)).thenReturn(new BudgetDTO());
        List<BudgetDTO> budgets = budgetService.getListBudgets(person.getId());
        assertNotNull(budgets);
        assertEquals(1, budgets.size());
    }

    @Test
    void getBudgetById() {
        when(transactionRepository.getTotalExpensesByCategoryAndPeriod(
                person.getId(), category.getId(), budget.getStartDate(), budget.getEndDate()))
                .thenReturn(new BigDecimal("200"));
        when(budgetRepositories.findByPersonIdAndId(person.getId(), budget.getId())).thenReturn(Optional.of(budget));
        when(modelMapper.map(budget, BudgetDTO.class)).thenReturn(new BudgetDTO());
        BudgetDTO result = budgetService.getBudgetById(budget.getId(), person.getId());
        assertNotNull(result);
        verify(budgetRepositories, times(1)).findByPersonIdAndId(person.getId(), budget.getId());
    }

    @Test
    void setBudgetByIdThrowException(){
        assertThrows(
                IllegalArgumentException.class,
                () -> budgetService.getBudgetById(10L, person.getId())
        );
    }


    @Test
    void save() {
        when(authPersonService.getCurrentPerson()).thenReturn(person);
        when(categoryRepository.findById(any())).thenReturn(Optional.of(category));

        when(transactionRepository.getTotalExpensesByCategoryAndPeriod(
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class))
        ).thenReturn(BigDecimal.ZERO);


        BudgetSaveDTO budgetSaveDTO = new BudgetSaveDTO();
        budgetSaveDTO.setCategoryId((long) category.getId());
        budgetSaveDTO.setAmount(new BigDecimal("500"));
        budgetSaveDTO.setStartDate(LocalDate.of(2025, 10, 1));




        Budget savedBudget = new Budget();
        savedBudget.setId(100L);
        savedBudget.setPerson(person);
        savedBudget.setCategory(category);
        savedBudget.setAmount(budgetSaveDTO.getAmount());
        savedBudget.setStartDate(budgetSaveDTO.getStartDate());
        savedBudget.setEndDate(budgetSaveDTO.getStartDate().plusDays(30));


        when(budgetRepositories.save(any(Budget.class))).thenReturn(savedBudget);

        BudgetDTO result = budgetService.save(budgetSaveDTO);
        assertNotNull(result); assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals((long) person.getId(), result.getPersonId());
        assertEquals("Food", result.getCategory());
        assertEquals(new BigDecimal("500"), result.getAmount());
        assertEquals(LocalDate.of(2025, 10, 1), result.getStartDate());
        assertEquals(LocalDate.of(2025, 10, 31), result.getEndDate());

        verify(budgetRepositories, times(1)).save(any(Budget.class));
        verify(authPersonService, times(1)).getCurrentPerson();
        verify(categoryRepository, times(1)).findById(1L);

    }

    @Test
    void update() {
        when(budgetRepositories.findById(anyLong())).thenReturn(Optional.of(budget));
        when(budgetRepositories.save(any(Budget.class))).thenReturn(budget);
        when(transactionRepository.getTotalExpensesByCategoryAndPeriod(
                anyInt(), anyInt(), any(LocalDate.class), any(LocalDate.class)
        )).thenReturn(BigDecimal.ZERO);

        BudgetUpdateDTO budgetUpdateDTO = new BudgetUpdateDTO();

        budgetUpdateDTO.setAmount(new BigDecimal("500"));
        budgetUpdateDTO.setEndDate(LocalDate.now().plusDays(35));

        BudgetDTO result = budgetService.update(budget.getId(), budgetUpdateDTO);
        assertNotNull(result);
        assertEquals(budget.getId(), result.getId());
        assertEquals(new BigDecimal("500"), result.getAmount());
        assertEquals(LocalDate.now().plusDays(35), result.getEndDate());

        assertEquals(new BigDecimal("500"), budget.getAmount());
        assertEquals(LocalDate.now().plusDays(35), budget.getEndDate());

        verify(budgetRepositories, times(1)).findById(budget.getId());
        verify(budgetRepositories, times(1)).save(budget);
    }

    @Test
    void delete() {
        budgetService.delete(budget.getId());
        verify(budgetRepositories, times(1)).deleteById(anyLong());
    }
}