package ru.abramov.FinFlow.FinFlow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private Person person;

    @BeforeEach
    void setUp() {
        category = new Category();
        person = new Person();
        category.setId(1);
        person.setId(1);

    }

    @Test
    void findById() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        Optional<Category> result = categoryService.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(category);
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void findAll() {
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category));
        List<Category> result = categoryService.findAll();
        assertThat(result).hasSize(1)
                        .containsExactly(category);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void findAllForPersonAndSystem() {
        Category category2 = new Category();
        Category categoryAnotherPerson = new Category();
        Person person2 = new Person();
        categoryAnotherPerson.setUser(person2);
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category, category2, categoryAnotherPerson));
        List<Category> result = categoryService.findAllForPersonAndSystem(person);
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void findAllForSystem() {
        Category category2 = new Category();
        Category category3 = new Category();
        Category categoryAnotherPerson = new Category();
        Person person2 = new Person();
        categoryAnotherPerson.setUser(person2);
        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category2, category3, categoryAnotherPerson));
        List<Category> result = categoryService.findAllForSystem();
        assertThat(result).hasSize(2);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void save() {
        categoryService.save(category);
        verify(categoryRepository, times(1)).save(category);
    }

    @Test
    void delete() {
        categoryService.delete(category);
        verify(categoryRepository, times(1)).delete(category);
    }
}