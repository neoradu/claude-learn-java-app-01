package com.example.expensetracker.repository;

import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedpassword");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @Test
    void save_and_findByEmail() {
        savedUser("test@example.com");

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
    }

    @Test
    void existsByEmail_returnsTrue_whenUserExists() {
        savedUser("test@example.com");

        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    void existsByEmail_returnsFalse_whenUserDoesNotExist() {
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void save_throwsException_whenDuplicateEmail() {
        savedUser("test@example.com");

        User duplicate = new User();
        duplicate.setEmail("test@example.com");
        duplicate.setPassword("anotherpassword");
        duplicate.setRole(Role.USER);

        assertThatThrownBy(() -> {
            userRepository.save(duplicate);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_setsTimestamps() {
        User user = savedUser("test@example.com");

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }
}
