package com.example.expensetracker.web;

import com.example.expensetracker.config.SecurityConfig;
import com.example.expensetracker.domain.Category;
import com.example.expensetracker.service.CategoryService;
import com.example.expensetracker.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {CategoryController.class, GlobalExceptionHandler.class})
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@WithMockUser
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void list_shouldRenderCategoriesPage() throws Exception {
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        when(categoryService.getAllCategories()).thenReturn(List.of(category));

        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/list"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void create_shouldReturnValidationErrors() throws Exception {
        mockMvc.perform(post("/categories")
                .with(csrf())
                .param("name", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("categories/form"))
            .andExpect(model().hasErrors());
    }

    @Test
    void create_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/categories")
                .with(csrf())
                .param("name", "Food")
                .param("description", "Food expenses"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/categories"));

        verify(categoryService).createCategory(any());
    }
}
