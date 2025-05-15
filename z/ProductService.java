package com.ecommerce.service;



import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProductService(ProductRepository productRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Page<Product> findByCategory(Category category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public Page<Product> findByNameContaining(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    public Page<Product> findInStock(Pageable pageable) {
        return productRepository.findByStockGreaterThan(0, pageable);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Product save(Product product, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            // Supprimer l'ancienne image si elle existe
            if (product.getId() != null) {
                Optional<Product> existingProduct = productRepository.findById(product.getId());
                if (existingProduct.isPresent() && existingProduct.get().getImageUrl() != null) {
                    fileStorageService.deleteFile(existingProduct.get().getImageUrl());
                }
            }

            // Enregistrer la nouvelle image
            String filename = fileStorageService.storeFile(image);
            product.setImageUrl(filename);
        }

        return productRepository.save(product);
    }

    public void deleteById(Long id) throws IOException {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().getImageUrl() != null) {
            fileStorageService.deleteFile(product.get().getImageUrl());
        }
        productRepository.deleteById(id);
    }
}