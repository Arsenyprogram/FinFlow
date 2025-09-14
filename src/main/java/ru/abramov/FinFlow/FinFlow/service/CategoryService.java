package ru.abramov.FinFlow.FinFlow.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.abramov.FinFlow.FinFlow.entity.Category;
import ru.abramov.FinFlow.FinFlow.entity.Person;
import ru.abramov.FinFlow.FinFlow.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findAllForPersonAndSystem(Person person){
        return categoryRepository.findAll().stream()
                .filter(category -> category.getUser() == null
                        || (category.getUser().getId() != null && category.getUser().getId().equals(person.getId())))
                .toList();
    }

    public List<Category> findAllForSystem(){
        return categoryRepository.findAll().stream()
                .filter(category -> category.getUser() == null)
                .toList();
    }



    @Transactional
    public void save(Category category) {
        categoryRepository.save(category);
    }

    @Transactional
    public void delete(Category category) {
        categoryRepository.delete(category);
    }

}
