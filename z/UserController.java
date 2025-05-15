package com.ecommerce.controller;

import com.ecommerce.model.User;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size) {

        Page<User> userPage = userService.findAll(PageRequest.of(page, size));

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("size", size);

        return "listu";
    }

    @GetMapping("/{id}")
    public String showUser(@PathVariable Long id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "showu"; // Fixed template name
        } else {
            return "redirect:/users";
        }
    }

    @GetMapping("/new")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "formu";
    }

    @PostMapping
    public String saveUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult bindingResult,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Create a new list of field errors that excludes the profileImage field errors
        boolean hasProfileImageError = bindingResult.hasFieldErrors("profileImage");

        if (bindingResult.hasErrors()) {
            // Check if the only errors are related to profileImage
            List<FieldError> nonProfileImageErrors = bindingResult.getFieldErrors()
                    .stream()
                    .filter(error -> !error.getField().equals("profileImage"))
                    .collect(Collectors.toList());

            // If there are other errors besides profileImage, return the form
            if (!nonProfileImageErrors.isEmpty()) {
                return "formu";
            }
        }

        try {
            // Pass the MultipartFile to the service for proper handling
            userService.save(user, profileImage);
            redirectAttributes.addFlashAttribute("successMessage", "User saved successfully!");
            return "redirect:/users";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Failed to upload profile image: " + e.getMessage());
            return "formu";
        }
    }

    @GetMapping("/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "formu";
        } else {
            return "redirect:/users";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete user: " + e.getMessage());
        }
        return "redirect:/users";
    }

    // Gestion des favoris
    @PostMapping("/{userId}/addFavorite/{productId}")
    public String addToFavorites(@PathVariable Long userId,
                                 @PathVariable Long productId,
                                 RedirectAttributes redirectAttributes) {
        userService.addToFavorites(userId, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Product added to favorites!");
        return "redirect:/products/" + productId;
    }

    @PostMapping("/{userId}/removeFavorite/{productId}")
    public String removeFromFavorites(@PathVariable Long userId,
                                      @PathVariable Long productId,
                                      RedirectAttributes redirectAttributes) {
        userService.removeFromFavorites(userId, productId);
        redirectAttributes.addFlashAttribute("successMessage", "Product removed from favorites!");
        return "redirect:/products/" + productId;
    }

    @GetMapping("/{id}/favorites")
    public String userFavorites(@PathVariable Long id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            model.addAttribute("favorites", user.get().getFavorites());
            return "favorites";
        } else {
            return "redirect:/users";
        }
    }
}