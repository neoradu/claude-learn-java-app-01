package com.example.expensetracker.web;

import com.example.expensetracker.domain.Expense;
import com.example.expensetracker.service.CategoryService;
import com.example.expensetracker.service.ExpenseService;
import com.example.expensetracker.web.form.ExpenseFilterForm;
import com.example.expensetracker.web.form.ExpenseForm;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    public ExpenseController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(@ModelAttribute("filter") ExpenseFilterForm filter,
                       @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<Expense> expenses = expenseService.getExpenses(filter, pageable);
        model.addAttribute("expenses", expenses);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "expenses/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("expenseForm", new ExpenseForm());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        return "expenses/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("expenseForm") ExpenseForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "expenses/form";
        }
        expenseService.createExpense(form);
        redirectAttributes.addFlashAttribute("successMessage", "Expense created successfully.");
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Expense expense = expenseService.getExpense(id);
        ExpenseForm form = new ExpenseForm();
        form.setDescription(expense.getDescription());
        form.setAmount(expense.getAmount());
        form.setDate(expense.getDate());
        form.setCategoryId(expense.getCategory().getId());

        model.addAttribute("expenseForm", form);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);
        model.addAttribute("expenseId", id);
        return "expenses/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("expenseForm") ExpenseForm form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            model.addAttribute("expenseId", id);
            return "expenses/form";
        }
        expenseService.updateExpense(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Expense updated successfully.");
        return "redirect:/expenses";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        expenseService.deleteExpense(id);
        redirectAttributes.addFlashAttribute("successMessage", "Expense deleted successfully.");
        return "redirect:/expenses";
    }
}
