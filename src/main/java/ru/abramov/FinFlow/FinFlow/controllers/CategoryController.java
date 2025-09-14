package ru.abramov.FinFlow.FinFlow.controllers;

import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final ModelMapper modelMapper;
    private final AuthPersonService authPersonService;

    public CategoryController(CategoryService categoryService, ModelMapper modelMapper, AuthPersonService authPersonService) {
        this.categoryService = categoryService;
        this.modelMapper = modelMapper;
        this.authPersonService = authPersonService;
    }

    @GetMapping()
    public ResponseEntity<List<CategoryDTO>> findAll() {
        Person person = authPersonService.getCurrentPerson();
        List<CategoryDTO> list = categoryService.findAllForPersonAndSystem(person).stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable Long id) {
        Category category = findCategory(id);
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return ResponseEntity.ok(categoryDTO);
    }

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
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Category category = findCategory(id);
        Person person = authPersonService.getCurrentPerson();
        person.getCategories().remove(category);
        categoryService.delete(category);
        return ResponseEntity.ok(HttpStatus.OK);
    }


    private Category findCategory(Long id) throws NoSuchElementException{
        return categoryService.findById(id).orElseThrow(
                () -> new NoSuchElementException("Категория с id=" + id + " не найдена"));
    }

    @GetMapping("/default")
    public ResponseEntity<List<CategoryDTO>> findDefault() {
        List<CategoryDTO> list = categoryService.findAllForSystem().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();
        return ResponseEntity.ok(list);
    }



}
