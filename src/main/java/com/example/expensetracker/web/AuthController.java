package com.example.expensetracker.web;

import com.example.expensetracker.service.DuplicateEmailException;
import com.example.expensetracker.service.UserService;
import com.example.expensetracker.web.form.SignupForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupForm", new SignupForm());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String register(@Valid @ModelAttribute("signupForm") SignupForm form,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
            return "auth/signup";
        }

        try {
            userService.register(form);
        } catch (DuplicateEmailException e) {
            bindingResult.rejectValue("email", "email.duplicate", e.getMessage());
            return "auth/signup";
        }

        return "redirect:/login?registered";
    }
}
