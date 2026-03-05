package com.example.expensetracker.web;

import com.example.expensetracker.domain.Category;
import com.example.expensetracker.service.CategoryInUseException;
import com.example.expensetracker.service.CategoryService;
import com.example.expensetracker.service.DuplicateCategoryException;
import com.example.expensetracker.service.ResourceNotFoundException;
import com.example.expensetracker.web.form.CategoryForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories/list";
    }

    @GetMapping("/categories/new")
    public String newForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        model.addAttribute("isEdit", false);
        return "categories/form";
    }

    @PostMapping("/categories")
    public String create(
        @Valid @ModelAttribute("categoryForm") CategoryForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "categories/form";
        }

        try {
            categoryService.createCategory(form);
        } catch (DuplicateCategoryException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            model.addAttribute("isEdit", false);
            return "categories/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Category created");
        return "redirect:/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategory(id);
        CategoryForm form = new CategoryForm();
        form.setName(category.getName());
        form.setDescription(category.getDescription());

        model.addAttribute("categoryId", id);
        model.addAttribute("categoryForm", form);
        model.addAttribute("isEdit", true);
        return "categories/form";
    }

    @PostMapping("/categories/{id}")
    public String update(
        @PathVariable Long id,
        @Valid @ModelAttribute("categoryForm") CategoryForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categoryId", id);
            model.addAttribute("isEdit", true);
            return "categories/form";
        }

        try {
            categoryService.updateCategory(id, form);
        } catch (DuplicateCategoryException ex) {
            bindingResult.rejectValue("name", "duplicate", ex.getMessage());
            model.addAttribute("categoryId", id);
            model.addAttribute("isEdit", true);
            return "categories/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Category updated");
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted");
        } catch (CategoryInUseException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/categories";
    }
}
