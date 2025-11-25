package ru.abramov.FinFlow.FinFlow.service;

import org.hibernate.annotations.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(value = "category", key ="#id", unless = "#result == null")
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Cacheable(value = "listAllCategory")
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Cacheable(value = "listPersonCategory", key = "#person.id", unless = "#result == null")
    public List<Category> findAllForPersonAndSystem(Person person){
        return categoryRepository.findAll().stream()
                .filter(category -> category.getUser() == null
                        || (category.getUser().getId() != null && category.getUser().getId().equals(person.getId())))
                .toList();
    }

    @Cacheable(value = "listDefaultCategory", unless = "#result == null")
    public List<Category> findAllForSystem(){
        return categoryRepository.findAll().stream()
                .filter(category -> category.getUser() == null)
                .toList();
    }


    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "category", key = "#category.id"),
                @CacheEvict(value = "listAllCategory", allEntries = true),
                @CacheEvict(value = "listPersonCategory", key = "#category.user.id"),
            })
    public void save(Category category) {
        categoryRepository.save(category);
    }


    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "category", key = "#category.id"),
                    @CacheEvict(value = "listAllCategory", allEntries = true),
                    @CacheEvict(value = "listPersonCategory", key = "#category.user.id")
            })
    public void delete(Category category) {
        categoryRepository.delete(category);
    }


}
