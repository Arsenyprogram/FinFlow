package ru.abramov.FinFlow.FinFlow.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Budget Controller",
        description = "CRUD операции с бюджетами пользователя"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@Data
@RequestMapping("budgets")
public class BudgetController {
    private final BudgetService budgetService;
    private final AuthPersonService authPersonService;
    private final ModelMapper modelMapper;


    @Operation(
            summary = "Получить список бюджетов",
            description = "Возвращает все бюджеты текущего авторизованного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Список бюджетов успешно получен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping()
    public ResponseEntity<List<BudgetDTO>> getBudgets() {
        return ResponseEntity.ok(budgetService.getListBudgets(authPersonService.getCurrentPerson().getId()));
    }


    @Operation(
            summary = "Получить бюджет по ID",
            description = "Возвращает бюджет по идентификатору, если он принадлежит текущему пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Бюджет найден"),
                    @ApiResponse(responseCode = "404", description = "Бюджет не найден"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<BudgetDTO> getBudgetById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(id, authPersonService.getCurrentPerson().getId()));
    }

    @Operation(
            summary = "Создать новый бюджет",
            description = "Создает бюджет для текущего пользователя",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Бюджет успешно создан"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @PostMapping()
    public ResponseEntity<?> createBudget(@RequestBody @Valid BudgetSaveDTO budgetSaveDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(budgetSaveDTO);
        }

        BudgetDTO budgetDTO = budgetService.save(budgetSaveDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(budgetDTO);
    }

    @Operation(
            summary = "Обновить бюджет",
            description = "Обновляет бюджет текущего пользователя по ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Бюджет успешно обновлён"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации данных"),
                    @ApiResponse(responseCode = "404", description = "Бюджет не найден"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable("id") Long id, @RequestBody  @Valid BudgetUpdateDTO budgetUpdateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(budgetUpdateDTO);
        }
        BudgetDTO budgetDTO = budgetService.update(id, budgetUpdateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetDTO);
    }


    @Operation(
            summary = "Удалить бюджет",
            description = "Удаляет бюджет по ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Бюджет успешно удалён"),
                    @ApiResponse(responseCode = "404", description = "Бюджет не найден"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable("id") Long id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
