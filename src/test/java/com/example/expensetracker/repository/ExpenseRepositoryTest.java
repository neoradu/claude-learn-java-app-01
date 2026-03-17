package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ExpenseRepositoryTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Category savedCategory() {
        Category category = new Category();
        category.setName("Food");
        return categoryRepository.save(category);
    }

    private User savedUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedpassword");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private Expense savedExpense(Category category, User owner) {
        Expense expense = new Expense();
        expense.setDescription("Lunch");
        expense.setAmount(new BigDecimal("12.50"));
        expense.setDate(LocalDate.now());
        expense.setCategory(category);
        expense.setOwner(owner);
        return expenseRepository.save(expense);
    }

    @Test
    void save_and_findById() {
        Category category = savedCategory();
        User owner = savedUser();
        Expense expense = savedExpense(category, owner);

        Optional<Expense> found = expenseRepository.findById(expense.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Lunch");
        assertThat(found.get().getAmount()).isEqualByComparingTo("12.50");
    }

    @Test
    void update_changesDescription() {
        Category category = savedCategory();
        User owner = savedUser();
        Expense expense = savedExpense(category, owner);

        expense.setDescription("Dinner");
        expenseRepository.save(expense);

        assertThat(expenseRepository.findById(expense.getId()).get().getDescription()).isEqualTo("Dinner");
    }

    @Test
    void delete_removesExpense() {
        Category category = savedCategory();
        User owner = savedUser();
        Expense expense = savedExpense(category, owner);
        Long id = expense.getId();

        expenseRepository.delete(expense);

        assertThat(expenseRepository.findById(id)).isEmpty();
    }

    @Test
    void existsByCategoryId_returnsTrue_whenExpenseExists() {
        Category category = savedCategory();
        User owner = savedUser();
        savedExpense(category, owner);

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
        User owner = savedUser();
        savedExpense(category, owner);
        savedExpense(category, owner);

        SessionFactory sessionFactory = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        Page<Expense> page = expenseRepository.findAll(Specification.where(null), PageRequest.of(0, 20));

        page.getContent().forEach(e -> assertThat(e.getCategory().getName()).isNotNull());

        assertThat(stats.getPrepareStatementCount()).isLessThanOrEqualTo(2);
    }
}
