package com.example.expensetracker.web;

import com.example.expensetracker.config.SecurityConfig;
import com.example.expensetracker.service.DuplicateEmailException;
import com.example.expensetracker.service.UserDetailsServiceImpl;
import com.example.expensetracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

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
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeExists("signupForm"));
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

    @Test
    void register_shouldRedirectToLogin_onSuccess() throws Exception {
        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("email", "new@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login?registered"));

        verify(userService).register(any());
    }

    @Test
    void register_shouldShowError_whenPasswordsMismatch() throws Exception {
        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("email", "new@example.com")
                .param("password", "password123")
                .param("confirmPassword", "different"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "confirmPassword"));
    }

    @Test
    void register_shouldShowError_whenEmailDuplicate() throws Exception {
        doThrow(new DuplicateEmailException("An account with this email already exists"))
            .when(userService).register(any());

        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("email", "existing@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "email"));
    }

    @Test
    void register_shouldShowError_whenEmailInvalid() throws Exception {
        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("email", "not-an-email")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "email"));
    }

    @Test
    void register_shouldShowError_whenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/signup")
                .with(csrf())
                .param("email", "new@example.com")
                .param("password", "short")
                .param("confirmPassword", "short"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/signup"))
            .andExpect(model().attributeHasFieldErrors("signupForm", "password"));
    }
}
