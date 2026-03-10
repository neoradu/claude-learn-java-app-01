package com.example.expensetracker.service;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Expense;
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
    public Page<Expense> getExpenses(ExpenseFilterForm filter, Pageable pageable) {
        Specification<Expense> spec = Specification.where(null);
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
    public Expense getExpense(Long id) {
        return expenseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    @Transactional
    public Expense createExpense(ExpenseForm form) {
        Category category = categoryRepository.findById(form.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + form.getCategoryId()));

        Expense expense = new Expense();
        expense.setDescription(form.getDescription().trim());
        expense.setAmount(form.getAmount());
        expense.setDate(form.getDate());
        expense.setCategory(category);
        return expenseRepository.save(expense);
    }

    @Transactional
    public Expense updateExpense(Long id, ExpenseForm form) {
        Expense expense = getExpense(id);
        Category category = categoryRepository.findById(form.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + form.getCategoryId()));

        expense.setDescription(form.getDescription().trim());
        expense.setAmount(form.getAmount());
        expense.setDate(form.getDate());
        expense.setCategory(category);
        return expenseRepository.save(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        expenseRepository.delete(getExpense(id));
    }
}
