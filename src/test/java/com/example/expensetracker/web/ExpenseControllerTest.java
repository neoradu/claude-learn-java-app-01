package com.example.expensetracker.web;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.service.CategoryService;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.service.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {ExpenseController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private CategoryService categoryService;

    private Category testCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        return category;
    }

    private Expense testExpense() {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription("Lunch");
        expense.setAmount(new BigDecimal("12.50"));
        expense.setDate(LocalDate.now());
        expense.setCategory(testCategory());
        return expense;
    }

    @Test
    void list_shouldRenderExpensesPage() throws Exception {
        when(expenseService.getExpenses(any(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(testExpense())));
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategory()));

        mockMvc.perform(get("/expenses"))
            .andExpect(status().isOk())
            .andExpect(view().name("expenses/list"))
            .andExpect(model().attributeExists("expenses"));
    }

    @Test
    void newForm_shouldRenderFormWithCategories() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategory()));

        mockMvc.perform(get("/expenses/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("expenses/form"))
            .andExpect(model().attributeExists("categories"));
    }

    @Test
    void create_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/expenses")
                .param("description", "Lunch")
                .param("amount", "12.50")
                .param("date", LocalDate.now().toString())
                .param("categoryId", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/expenses"));

        verify(expenseService).createExpense(any());
    }

    @Test
    void create_shouldReturnValidationErrors_whenDescriptionBlank() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategory()));

        mockMvc.perform(post("/expenses")
                .param("description", "")
                .param("amount", "12.50")
                .param("date", LocalDate.now().toString())
                .param("categoryId", "1"))
            .andExpect(status().isOk())
            .andExpect(view().name("expenses/form"))
            .andExpect(model().hasErrors());
    }

    @Test
    void editForm_shouldRenderFormWithExpenseData() throws Exception {
        when(expenseService.getExpense(1L)).thenReturn(testExpense());
        when(categoryService.getAllCategories()).thenReturn(List.of(testCategory()));

        mockMvc.perform(get("/expenses/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("expenses/form"))
            .andExpect(model().attributeExists("expenseForm"));
    }

    @Test
    void update_shouldRedirectOnSuccess() throws Exception {
        mockMvc.perform(post("/expenses/1")
                .param("description", "Dinner")
                .param("amount", "20.00")
                .param("date", LocalDate.now().toString())
                .param("categoryId", "1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/expenses"));

        verify(expenseService).updateExpense(eq(1L), any());
    }

    @Test
    void delete_shouldRedirectToList() throws Exception {
        mockMvc.perform(post("/expenses/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/expenses"));

        verify(expenseService).deleteExpense(1L);
    }

    @Test
    void editForm_shouldRedirect_whenExpenseNotFound() throws Exception {
        when(expenseService.getExpense(99L)).thenThrow(new ResourceNotFoundException("Expense not found: 99"));

        mockMvc.perform(get("/expenses/99/edit"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/expenses"));
    }
}
