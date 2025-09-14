package ru.abramov.FinFlow.FinFlow.controllers;

import jakarta.validation.Valid;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetSaveDTO;
import ru.abramov.FinFlow.FinFlow.dto.Budget.BudgetUpdateDTO;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.BudgetService;

import java.util.List;

@RestController
@Data
@RequestMapping("budgets")
public class BudgetController {
    private final BudgetService budgetService;
    private final AuthPersonService authPersonService;
    private final ModelMapper modelMapper;

    @GetMapping()
    public ResponseEntity<List<BudgetDTO>> getBudgets() {
        return ResponseEntity.ok(budgetService.getListBudgets(authPersonService.getCurrentPerson().getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id, authPersonService.getCurrentPerson().getId()));
    }

    @PostMapping()
    public ResponseEntity<?> createBudget(@RequestBody @Valid BudgetSaveDTO budgetSaveDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(budgetSaveDTO);
        }

        BudgetDTO budgetDTO = budgetService.save(budgetSaveDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(budgetDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable("id") Long id, @RequestBody  @Valid BudgetUpdateDTO budgetUpdateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(budgetUpdateDTO);
        }
        BudgetDTO budgetDTO = budgetService.update(id, budgetUpdateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetDTO);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable("id") Long id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
