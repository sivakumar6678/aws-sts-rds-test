package com.example.ticketapprds.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username cannot be empty")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Column(nullable = false)
    private String password;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email cannot be empty")
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String role = "ROLE_USER";
    
    @Column(name = "full_name", nullable = false)
    private String fullName = "";
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    // Method to update fullName when firstName or lastName changes
    @PrePersist
    @PreUpdate
    private void updateFullName() {
        this.fullName = (firstName != null ? firstName : "") + " " + 
                       (lastName != null ? lastName : "").trim();
        if (this.fullName.trim().isEmpty()) {
            this.fullName = username; // Use username as fallback
        }
    }
}
