package com.ecommerce.service;


import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserService(UserRepository userRepository, ProductRepository productRepository, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User save(User user, MultipartFile profileImage) throws IOException {
        if (profileImage != null && !profileImage.isEmpty()) {
            // Supprimer l'ancienne image si elle existe
            if (user.getId() != null) {
                Optional<User> existingUser = userRepository.findById(user.getId());
                if (existingUser.isPresent() && existingUser.get().getProfileImage() != null) {
                    fileStorageService.deleteFile(existingUser.get().getProfileImage());
                }
            }

            // Enregistrer la nouvelle image
            String filename = fileStorageService.storeFile(profileImage);
            user.setProfileImage(filename);
        }

        return userRepository.save(user);
    }

    public void deleteById(Long id) throws IOException {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent() && user.get().getProfileImage() != null) {
            fileStorageService.deleteFile(user.get().getProfileImage());
        }
        userRepository.deleteById(id);
    }

    // Méthode pour ajouter un produit aux favoris
    public void addToFavorites(Long userId, Long productId) {
        userRepository.findById(userId).ifPresent(user -> {
            productRepository.findById(productId).ifPresent(product -> {
                user.getFavorites().add(product);
                userRepository.save(user);
            });
        });
    }

    // Méthode pour supprimer un produit des favoris
    public void removeFromFavorites(Long userId, Long productId) {
        userRepository.findById(userId).ifPresent(user -> {
            productRepository.findById(productId).ifPresent(product -> {
                user.getFavorites().remove(product);
                userRepository.save(user);
            });
        });
    }
}