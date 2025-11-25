package ru.abramov.FinFlow.FinFlow.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.abramov.FinFlow.FinFlow.dto.Analytics.*;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.service.AnalyticsService;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
@Tag(
        name = "Analytics",
        description = "Эндпоинты для получения финансовой аналитики пользователя"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AuthPersonService authPersonService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService, AuthPersonService authPersonService) {
        this.analyticsService = analyticsService;
        this.authPersonService = authPersonService;
    }

    @Operation(
            summary = "Текущий баланс пользователя",
            description = "Возвращает текущий баланс авторизованного пользователя",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Баланс успешно получен"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("balance/current")
    public ResponseEntity<BigDecimal> getCurrentBalance() {
        Person person = authPersonService.getCurrentPerson();
        double amount = analyticsService.getCurrentBalance(person);
        return ResponseEntity.ok(BigDecimal.valueOf(amount));
    }


    @Operation(
            summary = "История баланса",
            description = "Возвращает историю баланса пользователя за указанный период",
            responses = {
                    @ApiResponse(responseCode = "200", description = "История баланса успешно получена"),
                    @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/balance/history")
    public ResponseEntity<?> getBalanceHistory(
            @RequestParam(defaultValue = "month") String period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        int userId = authPersonService.getCurrentPerson().getId();
        if (end.isBefore(start)) {
            return ResponseEntity.badRequest().body("end must be >= start");
        }

        PeriodUnit unit;
        try {
            unit = PeriodUnit.fromString(period);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Unknown period: " + period);
        }

        BalanceHistoryResponse resp = analyticsService.getBalanceHistory(userId, unit, start, end);
        return ResponseEntity.ok(resp);
    }

    @Operation(
            summary = "Расходы по категориям",
            description = "Возвращает расходы пользователя по категориям за указанный месяц",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные успешно получены"),
                    @ApiResponse(responseCode = "400", description = "Некорректный месяц"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("expenses/by-category")
    public ResponseEntity<?> getExpensesByCategory(
            @RequestParam("month") String monthStr){
        Person user = authPersonService.getCurrentPerson();
        YearMonth month = YearMonth.parse(monthStr);
        List<ExpenseByCategoryDTO> result = analyticsService.getExpenseByCategory(user.getId(), month);
        return ResponseEntity.ok(result);
    }


    @Operation(
            summary = "Доходы vs Расходы",
            description = "Возвращает сравнительную статистику доходов и расходов за указанный год",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Данные успешно получены"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/income-vs-expenses")
    public ResponseEntity<IncomeVsExpensesDTO> getIncomeVsExpenses(@RequestParam int year) {
        return ResponseEntity.ok(analyticsService.getIncomeVsExpenses(year));
    }

    @Operation(
            summary = "Ежемесячная сводка",
            description = "Возвращает сводку доходов и расходов за указанный месяц",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Сводка успешно получена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован"),
                    @ApiResponse(responseCode = "400", description = "Некорректный месяц")
            }
    )
    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthStaticDTO> getMonthlySummary(@RequestParam("month") String monthStr) {
        return ResponseEntity.ok(analyticsService.getMonthStatic(monthStr));
    }

}
