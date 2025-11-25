package ru.abramov.FinFlow.FinFlow.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.abramov.FinFlow.FinFlow.dto.Category.CategoryDTO;
import ru.abramov.FinFlow.FinFlow.dto.Category.CategorySaveDTO;
import ru.abramov.FinFlow.FinFlow.dto.Category.CategoryUpdateDTO;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.service.AuthPersonService;
import ru.abramov.FinFlow.FinFlow.service.CategoryService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
@Tag(
        name = "Categories",
        description = "Эндпоинты для управления категориями доходов и расходов"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ModelMapper modelMapper;
    private final AuthPersonService authPersonService;


    @Operation(
            summary = "Получить все категории пользователя",
            description = "Возвращает личные категории авторизованного пользователя и системные категории",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Категории успешно получены"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping()
    public ResponseEntity<List<CategoryDTO>> findAll() {
        Person person = authPersonService.getCurrentPerson();
        List<CategoryDTO> list = categoryService.findAllForPersonAndSystem(person).stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(
            summary = "Получить категорию по ID",
            description = "Возвращает категорию, если она принадлежит пользователю или является системной",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Категория успешно получена"),
                    @ApiResponse(responseCode = "404", description = "Категория не найдена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable Long id) {
        Category category = findCategory(id);
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return ResponseEntity.ok(categoryDTO);
    }


    @Operation(
            summary = "Создать категорию",
            description = "Создает новую личную категорию для пользователя",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Категория успешно создана"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные категории"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @PostMapping
    public ResponseEntity<?> save(@RequestBody  @Valid CategorySaveDTO categorySaveDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Person person = authPersonService.getCurrentPerson();
        Category category = modelMapper.map(categorySaveDTO, Category.class);
        category.setUser(person);
        person.getCategories().add(category);
        category.setCreated_at(Timestamp.valueOf(LocalDateTime.now()));
        categoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Обновить категорию",
            description = "Изменяет название или описание категории",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Категория успешно обновлена"),
                    @ApiResponse(responseCode = "400", description = "Некорректные данные"),
                    @ApiResponse(responseCode = "404", description = "Категория не найдена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody  @Valid CategoryUpdateDTO categoryUpdateDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        Category category = findCategory(id);
        modelMapper.map(categoryUpdateDTO, category);
        category.setUpdated_at(Timestamp.valueOf(LocalDateTime.now()));
        categoryService.save(category);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @Operation(
            summary = "Удалить категорию",
            description = "Удаление категории пользователя. Системные категории недоступны для удаления",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
                    @ApiResponse(responseCode = "404", description = "Категория не найдена"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Category category = findCategory(id);
        Person person = authPersonService.getCurrentPerson();
        person.getCategories().remove(category);
        categoryService.delete(category);
        return ResponseEntity.ok(HttpStatus.OK);
    }



    @Operation(
            summary = "Получить системные категории",
            description = "Список базовых категорий, доступных каждому пользователю",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Категории успешно получены")
            }
    )
    @GetMapping("/default")
    public ResponseEntity<List<CategoryDTO>> findDefault() {
        List<CategoryDTO> list = categoryService.findAllForSystem().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return ResponseEntity.ok(list);
    }


    private Category findCategory(Long id) throws NoSuchElementException{
        return categoryService.findById(id).orElseThrow(
                () -> new NoSuchElementException("Категория с id=" + id + " не найдена"));
    }

}
