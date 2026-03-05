package com.example.expensetracker.service;

import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.web.form.CategoryForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;



    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_shouldRejectDuplicateNameCaseInsensitive() {
        CategoryForm form = new CategoryForm();
        form.setName("Food");
        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(form))
            .isInstanceOf(DuplicateCategoryException.class);
    }

}
