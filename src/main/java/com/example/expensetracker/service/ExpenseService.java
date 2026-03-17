package com.example.expensetracker.service;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.web.form.ExpenseFilterForm;
import com.example.expensetracker.web.form.ExpenseForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<Expense> getExpenses(ExpenseFilterForm filter, Pageable pageable, User currentUser) {
        Specification<Expense> spec = Specification.where(null);

        if (currentUser.getRole() != Role.ADMIN) {
            spec = spec.and(ExpenseSpecification.hasOwner(currentUser.getId()));
        }

        if (filter.getCategoryId() != null) {
            spec = spec.and(ExpenseSpecification.hasCategory(filter.getCategoryId()));
        }
        if (filter.getDateFrom() != null) {
            spec = spec.and(ExpenseSpecification.dateFrom(filter.getDateFrom()));
        }
        if (filter.getDateTo() != null) {
            spec = spec.and(ExpenseSpecification.dateTo(filter.getDateTo()));
        }
        if (filter.getMinAmount() != null) {
            spec = spec.and(ExpenseSpecification.minAmount(filter.getMinAmount()));
        }
        if (filter.getMaxAmount() != null) {
            spec = spec.and(ExpenseSpecification.maxAmount(filter.getMaxAmount()));
        }
        return expenseRepository.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Expense getExpense(Long id, User currentUser) {
        Expense expense = expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        checkOwnership(expense, currentUser);
        return expense;
    }

    @Transactional
    public Expense createExpense(ExpenseForm form, User currentUser) {
        Category category = categoryRepository.findById(form.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + form.getCategoryId()));

        Expense expense = new Expense();
        expense.setDescription(form.getDescription().trim());
        expense.setAmount(form.getAmount());
        expense.setDate(form.getDate());
        expense.setCategory(category);
        expense.setOwner(currentUser);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(Long id, ExpenseForm form, User currentUser) {
        Expense expense = getExpense(id, currentUser);
        Category category = categoryRepository.findById(form.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + form.getCategoryId()));

        expense.setDescription(form.getDescription().trim());
        expense.setAmount(form.getAmount());
        expense.setDate(form.getDate());
        expense.setCategory(category);
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id, User currentUser) {
        expenseRepository.delete(getExpense(id, currentUser));
    }

    private void checkOwnership(Expense expense, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (expense.getOwner() == null
                || !expense.getOwner().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Expense not found: " + expense.getId());
        }
    }
}
