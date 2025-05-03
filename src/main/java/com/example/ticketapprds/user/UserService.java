package com.example.ticketapprds.user;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> otpStorage = new java.util.concurrent.ConcurrentHashMap<>();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default role if not specified
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        
        // Save the user
        return userRepository.save(user);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void saveOtpForUser(String username, String otp) {
        System.out.println("Saving OTP for user: " + username + ", OTP: " + otp);
        otpStorage.put(username, otp);
    }

    public boolean verifyOtp(String username, String otp) {
        String storedOtp = otpStorage.get(username);
        System.out.println("Verifying OTP for user: " + username);
        System.out.println("Entered OTP: " + otp);
        System.out.println("Stored OTP: " + storedOtp);
        
        if (storedOtp == null) {
            System.out.println("No OTP found for user: " + username);
            return false;
        }
        
        boolean isValid = storedOtp.equals(otp);
        System.out.println("OTP valid: " + isValid);
        
        // If valid, remove the OTP so it can't be reused
        if (isValid) {
            otpStorage.remove(username);
        }
        
        return isValid;
    }

    public String getEmailByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getEmail();
    }
    
    public void authenticateUserAfterOtpVerification(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        System.out.println("Authenticating user after OTP verification: " + username);
        System.out.println("User role: " + user.getRole());
        
        try {
            // Create authentication token with user's role
            Authentication auth = new UsernamePasswordAuthenticationToken(
                username, 
                null, // No credentials needed here
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
            );
            
            // Create a new security context
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(auth);
            
            // Set the security context in the holder
            SecurityContextHolder.setContext(securityContext);
            
            // Get the current request and session
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            HttpSession session = request.getSession(true);
            
            // Store the security context in the session
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            
            System.out.println("Session ID: " + session.getId());
            System.out.println("Security context stored in session");
            
            // Verify authentication was set
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current authentication: " + (currentAuth != null ? currentAuth.getName() : "null"));
            System.out.println("Is authenticated: " + (currentAuth != null && currentAuth.isAuthenticated()));
            if (currentAuth != null) {
                System.out.println("Authorities: " + currentAuth.getAuthorities());
            }
            
            System.out.println("User authenticated after OTP verification: " + username);
        } catch (Exception e) {
            System.out.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
        }
    }
}