package com.ecommerce.repository;

import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Recherche de produits par catégorie avec pagination
    Page<Product> findByCategory(Category category, Pageable pageable);

    // Recherche de produits par nom contenant une chaîne (pour la recherche)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Recherche de produits par fourchette de prix
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Recherche de produits en stock
    Page<Product> findByStockGreaterThan(Integer stock, Pageable pageable);
}