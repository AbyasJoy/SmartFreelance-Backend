package com.smartfreelance.service;

import com.smartfreelance.entity.Category;
import com.smartfreelance.exception.ResourceNotFoundException;
import com.smartfreelance.repository.CategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public Category createCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        return categoryRepository.save(category);
    }

    @Cacheable(value = "categories")
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Cacheable(value = "categories", key = "#id")
    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public Category updateCategory(Long id, Category categoryRequest) {
        Category category = getCategoryById(id);
        if (categoryRequest.getName() != null && !categoryRequest.getName().trim().isEmpty()) {
            category.setName(categoryRequest.getName());
        }
        if (categoryRequest.getDescription() != null) {
            category.setDescription(categoryRequest.getDescription());
        }
        return categoryRepository.save(category);
    }

    @CacheEvict(value = "categories", allEntries = true)
    @Transactional
    public Category updateCategoryStatus(Long id, boolean active) {
        Category category = getCategoryById(id);
        category.setActive(active);
        return categoryRepository.save(category);
    }
}
