package com.example.expensetracker.config;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.user-password:password}")
    private String seedUserPassword;

    @Value("${app.seed.admin-password:password}")
    private String seedAdminPassword;

    public DataInitializer(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           ExpenseRepository expenseRepository,
                           PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedCategories();
        seedUsers();
        assignOrphanedExpenses();
    }

    private void seedCategories() {
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

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }

        log.warn("Seeding default users — change passwords via app.seed.user-password / app.seed.admin-password for non-dev environments");

        User defaultUser01 = new User();
        defaultUser01.setEmail("user01@example.com");
        defaultUser01.setPassword(passwordEncoder.encode(seedUserPassword));
        defaultUser01.setRole(Role.USER);
        userRepository.save(defaultUser01);

        User defaultUser02 = new User();
        defaultUser02.setEmail("user02@example.com");
        defaultUser02.setPassword(passwordEncoder.encode(seedUserPassword));
        defaultUser02.setRole(Role.USER);
        userRepository.save(defaultUser02);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode(seedAdminPassword));
        adminUser.setRole(Role.ADMIN);
        userRepository.save(adminUser);
    }

    private void assignOrphanedExpenses() {
        userRepository.findByEmail("user01@example.com").ifPresent(defaultUser ->
            expenseRepository.assignOrphanedExpensesToOwner(defaultUser)
        );
    }
}
