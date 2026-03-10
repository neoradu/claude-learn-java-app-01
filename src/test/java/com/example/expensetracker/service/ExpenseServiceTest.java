package com.example.expensetracker.service;

import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.web.form.ExpenseForm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private ExpenseForm validForm() {
        ExpenseForm form = new ExpenseForm();
        form.setDescription("Lunch");
        form.setAmount(new BigDecimal("10.00"));
        form.setDate(LocalDate.now());
        form.setCategoryId(99L);
        return form;
    }

    @Test
    void createExpense_throwsResourceNotFoundException_whenCategoryNotFound() {
        ExpenseForm form = validForm();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(form))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getExpense_throwsResourceNotFoundException_whenNotFound() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpense(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteExpense_throwsResourceNotFoundException_whenNotFound() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
