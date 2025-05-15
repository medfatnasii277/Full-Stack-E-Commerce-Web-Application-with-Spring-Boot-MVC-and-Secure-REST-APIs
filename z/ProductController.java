package com.ecommerce.controller;


import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listProducts(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) Long categoryId) {

        Page<Product> productPage;

        if (categoryId != null) {
            Optional<Category> categoryOpt = categoryService.findById(categoryId);
            if (categoryOpt.isPresent()) {
                productPage = productService.findByCategory(categoryOpt.get(), PageRequest.of(page, size));
                model.addAttribute("categoryId", categoryId);
            } else {
                productPage = productService.findAll(PageRequest.of(page, size));
            }
        } else {
            productPage = productService.findAll(PageRequest.of(page, size));
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("size", size);

        List<Category> categories = categoryService.findAll();
        model.addAttribute("categories", categories);

        return "list";
    }

    @GetMapping("/{id}")
    public String showProduct(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "show";
        } else {
            return "redirect:/products";
        }
    }

    @GetMapping("/new")
    public String createProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        return "form";
    }

    @PostMapping
    public String saveProduct(@Valid @ModelAttribute Product product,
                              BindingResult bindingResult,
                              @RequestParam(value = "image", required = false) MultipartFile image,
                              @RequestParam("categoryId") Long categoryId,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "form";
        }

        try {
            // Associer la catégorie au produit
            categoryService.findById(categoryId).ifPresent(product::setCategory);

            // Enregistrer le produit avec l'image
            productService.save(product, image);
            redirectAttributes.addFlashAttribute("successMessage", "Product saved successfully!");
            return "redirect:/products";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to upload image: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAll());
            return "form";
        }
    }

    @GetMapping("/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            model.addAttribute("categories", categoryService.findAll());
            return "form";
        } else {
            return "redirect:/products";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete product: " + e.getMessage());
        }
        return "redirect:/products";
    }

}