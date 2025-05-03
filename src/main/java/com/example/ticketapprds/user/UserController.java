package com.example.ticketapprds.user;

import com.example.ticketapprds.user.dto.UserRegistrationDto;
import com.example.ticketapprds.user.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
public class UserController {

    private final UserService userService;
    private final EmailService emailService;

    public UserController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }
    
    @GetMapping("/home")
    public String home() {
        return "redirect:/tickets";
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

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model) {
        try {
            // Trim inputs
            username = username.trim();
            
            System.out.println("Login attempt for user: " + username);
            
            // Authenticate user
            if (userService.authenticate(username, password)) {
                System.out.println("User authenticated: " + username);

                // Generate OTP
                String otp = generateOtp();
                userService.saveOtpForUser(username, otp);

                // Send OTP to user's email
                String email = userService.getEmailByUsername(username);
                emailService.sendOtpEmail(email, otp);
                System.out.println("OTP sent to email: " + email);

                // Redirect to OTP verification page
                model.addAttribute("username", username);
                return "otp-verification";
            } else {
                System.out.println("Authentication failed for user: " + username);
                model.addAttribute("error", "Invalid username or password");
                return "login";
            }
        } catch (Exception e) {
            System.out.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/verify-otp")
    public String showOtpVerificationPage(@RequestParam(value = "username", required = false) String username, Model model) {
        if (username == null || username.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        return "otp-verification";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("username") String username,
                            @RequestParam("otp") String otp,
                            Model model,
                            HttpServletRequest request) {
        System.out.println("Verifying OTP for user: " + username + ", OTP: " + otp);
        
        // Trim the OTP to remove any whitespace
        otp = otp.trim();
        
        if (userService.verifyOtp(username, otp)) {
            System.out.println("OTP verification successful for user: " + username);
            
            try {
                // Authenticate the user in the security context
                userService.authenticateUserAfterOtpVerification(username);
                
                // Get the current session
                HttpSession session = request.getSession(true);
                System.out.println("Session ID in controller: " + session.getId());
                
                // Verify authentication was set
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                System.out.println("Authentication in controller: " + (auth != null ? auth.getName() : "null"));
                
                // Return the ticket view directly instead of redirecting
                return "redirect:/tickets";
            } catch (Exception e) {
                System.out.println("Error during authentication after OTP verification: " + e.getMessage());
                e.printStackTrace();
                
                // If there's an error, still try to show the ticket page
                return "redirect:/tickets";
            }
        } else {
            System.out.println("OTP verification failed for user: " + username);
            model.addAttribute("error", "Invalid OTP. Please try again.");
            model.addAttribute("username", username);
            return "otp-verification";
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        String otpString = String.valueOf(otp);
        
        // Ensure it's exactly 6 digits
        while (otpString.length() < 6) {
            otpString = "0" + otpString;
        }
        
        System.out.println("Generated OTP: " + otpString);
        return otpString;
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