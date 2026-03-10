package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Expense;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    private Category savedCategory() {
        Category category = new Category();
        category.setName("Food");
        return categoryRepository.save(category);
    }

    private Expense savedExpense(Category category) {
        Expense expense = new Expense();
        expense.setDescription("Lunch");
        expense.setAmount(new BigDecimal("12.50"));
        expense.setDate(LocalDate.now());
        expense.setCategory(category);
        return expenseRepository.save(expense);
    }

    @Test
    void save_and_findById() {
        Category category = savedCategory();
        Expense expense = savedExpense(category);

        Optional<Expense> found = expenseRepository.findById(expense.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Lunch");
        assertThat(found.get().getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void update_changesDescription() {
        Category category = savedCategory();
        Expense expense = savedExpense(category);

        expense.setDescription("Dinner");
        expenseRepository.save(expense);

        assertThat(expenseRepository.findById(expense.getId()).get().getDescription()).isEqualTo("Dinner");
    }

    @Test
    void delete_removesExpense() {
        Category category = savedCategory();
        Expense expense = savedExpense(category);
        Long id = expense.getId();

        expenseRepository.delete(expense);

        assertThat(expenseRepository.findById(id)).isEmpty();
    }

    @Test
    void existsByCategoryId_returnsTrue_whenExpenseExists() {
        Category category = savedCategory();
        savedExpense(category);

        assertThat(expenseRepository.existsByCategoryId(category.getId())).isTrue();
    }

    @Test
    void existsByCategoryId_returnsFalse_whenNoExpenseForCategory() {
        Category category = savedCategory();

        assertThat(expenseRepository.existsByCategoryId(category.getId())).isFalse();
    }

    @Test
    void findAll_withEntityGraph_loadsCategory_withoutNPlusOne() {
        Category category = savedCategory();
        savedExpense(category);
        savedExpense(category);

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        Page<Expense> page = expenseRepository.findAll(Specification.where(null), PageRequest.of(0, 20));

        // Access category on each expense — must not trigger additional queries
        page.getContent().forEach(e -> assertThat(e.getCategory().getName()).isNotNull());

        // With @EntityGraph, category is loaded via JOIN in the data query.
        // Total statements: 1 data query + 1 count query = 2
        assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }
}
