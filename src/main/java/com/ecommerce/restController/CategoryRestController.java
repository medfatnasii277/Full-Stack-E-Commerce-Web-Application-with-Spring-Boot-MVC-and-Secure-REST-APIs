package com.ecommerce.restController;


import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.service.CategoryService;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Category API", description = "API endpoints for category management")
public class CategoryRestController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @Autowired
    public CategoryRestController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Returns a list of all categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Returns a category based on ID")
    @ApiResponse(responseCode = "200", description = "Category found",
            content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Category.class)) })
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Category> getCategoryById(@Parameter(description = "Category ID") @PathVariable Long id) {
        Optional<Category> category = categoryService.findById(id);
        return category.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/products")
    @Operation(summary = "Get products by category", description = "Returns products for a specific category")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {

        Optional<Category> category = categoryService.findById(id);
        if (category.isPresent()) {
            Page<Product> products = productService.findByCategory(category.get(), PageRequest.of(page, size));
            return ResponseEntity.ok(products);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new category", description = "Creates a new category")
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<Category> createCategory(
            @Parameter(description = "Category object") @Valid @RequestBody Category category) {

        Category savedCategory = categoryService.save(category);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Updates an existing category")
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Category> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Parameter(description = "Updated category object") @Valid @RequestBody Category category) {

        if (!categoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        category.setId(id);
        Category updatedCategory = categoryService.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Deletes a category")
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<Void> deleteCategory(@Parameter(description = "Category ID") @PathVariable Long id) {
        if (!categoryService.findById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }

        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}