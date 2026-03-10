package com.example.expensetracker.service;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.web.form.CategoryForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Transactional
    public Category createCategory(CategoryForm form) {
        String normalizedName = normalizeName(form.getName());
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateCategoryException("Category name already exists");
        }

        Category category = new Category();
        category.setName(normalizedName);
        category.setDescription(normalizeNullable(form.getDescription()));
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long id, CategoryForm form) {
        Category category = getCategory(id);
        String normalizedName = normalizeName(form.getName());
        categoryRepository.findByNameIgnoreCase(normalizedName)
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new DuplicateCategoryException("Category name already exists");
            });

        category.setName(normalizedName);
        category.setDescription(normalizeNullable(form.getDescription()));
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (expenseRepository.existsByCategoryId(id)) {
            throw new CategoryInUseException("Category is in use by one or more expenses.");
        }
        categoryRepository.delete(getCategory(id));
    }

    private String normalizeName(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
