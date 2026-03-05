package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void existsByNameIgnoreCase_shouldFindExistingName() {
        Category category = new Category();
        category.setName("Food");
        categoryRepository.save(category);

        boolean exists = categoryRepository.existsByNameIgnoreCase("food");
        assertThat(exists).isTrue();
    }
}
