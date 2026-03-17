package com.example.expensetracker.service;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseService expenseService;

    private User testUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.USER);
        return user;
    }

    private User adminUser() {
        User user = new User();
        user.setId(2L);
        user.setEmail("admin@example.com");
        user.setRole(Role.ADMIN);
        return user;
    }

    private Expense testExpense(User owner) {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Lunch");
        expense.setAmount(new BigDecimal("10.00"));
        expense.setDate(LocalDate.now());
        expense.setOwner(owner);
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        expense.setCategory(category);
        return expense;
    }

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
        User user = testUser();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.createExpense(form, user))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createExpense_setsOwnerToCurrentUser() {
        ExpenseForm form = validForm();
        User user = testUser();
        Category category = new Category();
        category.setId(99L);
        when(categoryRepository.findById(99L)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        expenseService.createExpense(form, user);

        verify(expenseRepository).save(argThat(expense ->
            expense.getOwner() != null && expense.getOwner().getId().equals(user.getId())
        ));
    }

    @Test
    void getExpense_throwsResourceNotFoundException_whenNotFound() {
        User user = testUser();
        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpense(1L, user))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getExpense_throwsResourceNotFoundException_whenNotOwner() {
        User owner = testUser();
        User otherUser = new User();
        otherUser.setId(99L);
        otherUser.setEmail("other@example.com");
        otherUser.setRole(Role.USER);

        Expense expense = testExpense(owner);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.getExpense(1L, otherUser))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getExpense_allowsAdmin_toAccessAnyExpense() {
        User owner = testUser();
        User admin = adminUser();

        Expense expense = testExpense(owner);
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));

        expenseService.getExpense(1L, admin);
    }

    @Test
    void deleteExpense_throwsResourceNotFoundException_whenNotFound() {
        User user = testUser();
        when(expenseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(1L, user))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
