package com.example.expensetracker.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AuthController.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_shouldRenderLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }

    @Test
    void signup_shouldRenderSignupPage() throws Exception {
        mockMvc.perform(get("/signup"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"));
    }

    @Test
    void loginPage_shouldContainLinkToSignup() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/signup")));
    }

    @Test
    void signupPage_shouldContainLinkToLogin() throws Exception {
        mockMvc.perform(get("/signup"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("/login")));
    }

    @Test
    void loginPage_shouldContainEmailAndPasswordInputs() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("type=\"email\"")))
            .andExpect(content().string(containsString("type=\"password\"")));
    }

    @Test
    void signupPage_shouldContainEmailAndPasswordInputs() throws Exception {
        mockMvc.perform(get("/signup"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("type=\"email\"")))
            .andExpect(content().string(containsString("type=\"password\"")));
    }
}
