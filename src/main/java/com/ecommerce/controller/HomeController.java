package com.ecommerce.controller;



import com.ecommerce.model.Product;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Récupérer les produits en stock pour affichage sur la page d'accueil
        Page<Product> featuredProducts = productService.findInStock(PageRequest.of(0, 8));
        model.addAttribute("featuredProducts", featuredProducts.getContent());

        // Récupérer toutes les catégories pour le menu de navigation
        model.addAttribute("categories", categoryService.findAll());

        return "home";
    }
}