package com.example.expensetracker.web;

import com.example.expensetracker.config.SecurityConfig;
import com.example.expensetracker.domain.Role;
import com.example.expensetracker.domain.User;
import com.example.expensetracker.service.CategoryService;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ExpenseController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ExpenseControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User testUser() {
        User u = new User();
        u.setId(1L);
        u.setEmail("user@example.com");
        u.setPassword("encoded");
        u.setRole(Role.USER);
        return u;
    }

    @Test
    void unauthenticatedGet_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/expenses"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void unauthenticatedPost_shouldNotBeAccessible() throws Exception {
        mockMvc.perform(post("/expenses"))
            .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedGet_shouldReturn200() throws Exception {
        when(expenseService.getExpenses(any(), any(Pageable.class), any(User.class)))
            .thenReturn(new PageImpl<>(List.of()));
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(get("/expenses").with(user(testUser())))
            .andExpect(status().isOk());
    }
}
