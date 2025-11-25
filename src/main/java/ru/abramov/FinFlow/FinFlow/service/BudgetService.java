package ru.abramov.FinFlow.FinFlow.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetSaveDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Budget;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.repository.BudgetRepositories;
import ru.abramov.FinFlow.FinFlow.repository.CategoryRepository;
import ru.abramov.FinFlow.FinFlow.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;


@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class BudgetService {

    private final BudgetRepositories budgetRepositories;
    private final ModelMapper modelMapper;
    private final TransactionRepository transactionRepository;
    private final AuthPersonService authPersonService;
    private final CategoryRepository categoryRepository;


    @Cacheable(value = "listBudgets", key = "#personId", unless = "#result == null")
    public List<BudgetDTO> getListBudgets(Integer personId) {
        List<Budget> budgets = budgetRepositories.findAllByPersonId(personId);

        return budgets.stream().map(budget ->
            getBudgetDTO(budget, personId)
        ).toList();
    }

    @Cacheable(value = "budget", key = "#id + '-' + #personId", unless = "#result == null")
    public BudgetDTO  getBudgetById(Long id, Integer personId) {
        Budget budget = budgetRepositories.findByPersonIdAndId(personId, id).orElseThrow(() -> new IllegalArgumentException("Неправильный id бюджета"));
        return getBudgetDTO(budget, personId);
    }


    private BudgetDTO getBudgetDTO(Budget budget, Integer personId) {
        BudgetDTO dto = modelMapper.map(budget, BudgetDTO.class);
        dto.setCategory(budget.getCategory().getName());

        BigDecimal currentSpending = transactionRepository
                .getTotalExpensesByCategoryAndPeriod(
                        personId,
                        budget.getCategory().getId(),
                        budget.getStartDate(),
                        budget.getEndDate()
                );

        BigDecimal remaining = budget.getAmount().subtract(currentSpending);

        BigDecimal utilizationPercentage = BigDecimal.ZERO;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            utilizationPercentage = currentSpending
                    .divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }


        String status;
        if (utilizationPercentage.compareTo(BigDecimal.valueOf(80)) < 0) {
            status = "Приемлимо";
        } else if (utilizationPercentage.compareTo(BigDecimal.valueOf(100)) <= 0) {
            status = "На грани";
        } else {
            status = "Превышено";
        }
        dto.setCurrentSpending(currentSpending);
        dto.setRemaining(remaining);
        dto.setUtilizationPercentage(utilizationPercentage);
        dto.setStatus(status);

        return dto;
    }

    @Transactional
    @CachePut(value = "budget", key = "#result.id + '-' + @authPersonService.getCurrentPerson().id")
    @CacheEvict(value = "listBudgets", key = "@authPersonService.getCurrentPerson().id")
    public BudgetDTO save(BudgetSaveDTO budgetSaveDTO) {
        try {
            Budget budget = new Budget();
            budget.setPerson(authPersonService.getCurrentPerson());

            Category category = categoryRepository.findById(budgetSaveDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Категория не найдена"));
            budget.setCategory(category);

            budget.setAmount(budgetSaveDTO.getAmount());
            budget.setStartDate(budgetSaveDTO.getStartDate());
            budget.setEndDate(budgetSaveDTO.getStartDate().plusDays(30));

            Budget savedBudget = budgetRepositories.save(budget);

            return getBudgetDTO(savedBudget, budget.getPerson().getId());
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Бюджет для этой категории на этот период уже существует");
        }
    }

    @Transactional
    @CachePut(value = "budget", key = "#id + '-' + @authPersonService.getCurrentPerson().id")
    @CacheEvict(value = "listBudgets", key = "@authPersonService.getCurrentPerson().id")
    public BudgetDTO update(Long id, BudgetUpdateDTO budgetUpdateDTO) {
        Budget budget = budgetRepositories.findById(id).orElseThrow(() -> new IllegalArgumentException("Такого id нет"));
        budget.setAmount(budgetUpdateDTO.getAmount());
        budget.setEndDate(budgetUpdateDTO.getEndDate());
        budgetRepositories.save(budget);
        return getBudgetDTO(budget, budget.getPerson().getId());
    }

    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "budget", key = "#id + '-' + @authPersonService.getCurrentPerson().id"),
                    @CacheEvict(value = "listBudgets", key = "@authPersonService.getCurrentPerson().id")
            }
    )
    public void delete(Long id) {
        budgetRepositories.deleteById(id);
    }

}
