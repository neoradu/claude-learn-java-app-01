package com.example.expensetracker.config;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public DataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<String> defaultCategories = List.of("Food", "Transport", "Utilities", "Health", "Other");
        defaultCategories.forEach(name -> {
            Category category = new Category();
            category.setName(name);
            categoryRepository.save(category);
        });
    }
}
