package com.example.ticketapprds.user;

import com.example.ticketapprds.user.dto.UserRegistrationDto;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model, 
                           @RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           @RequestParam(value = "registered", required = false) String registered) {
        
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }
        
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please log in.");
        }
        
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result, Model model) {
        
        // Check if passwords match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }
        
        // Check for validation errors
        if (result.hasErrors()) {
            return "register";
        }
        
        try {
            // Convert DTO to entity
            User user = new User();
            user.setUsername(registrationDto.getUsername());
            user.setFirstName(registrationDto.getFirstName());
            user.setLastName(registrationDto.getLastName());
            user.setEmail(registrationDto.getEmail());
            user.setPassword(registrationDto.getPassword());
            
            // Set the fullName field explicitly
            String fullName = (registrationDto.getFirstName() != null ? registrationDto.getFirstName() : "") + " " +
                             (registrationDto.getLastName() != null ? registrationDto.getLastName() : "").trim();
            if (fullName.trim().isEmpty()) {
                fullName = registrationDto.getUsername(); // Use username as fallback
            }
            user.setFullName(fullName);
            
            // Register the user
            userService.registerNewUser(user);
            
            // Redirect to login page with a success parameter
            return "redirect:/login?registered";
            
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}