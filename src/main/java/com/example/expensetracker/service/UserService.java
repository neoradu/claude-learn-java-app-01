package com.example.expensetracker.service;

import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.web.form.SignupForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(SignupForm form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new DuplicateEmailException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }
}
