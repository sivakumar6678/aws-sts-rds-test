package com.example.ticketapprds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class TicketController {

    @Autowired
    private TicketService ticketService;

    // Load Home Page
    @GetMapping("/")
    public String home(Model model, @RequestParam(value = "activeTab", required = false) String activeTab) {
        // Redirect to tickets page if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return "redirect:/tickets";
        }
        
        // Otherwise redirect to login page
        return "redirect:/login";
    }
    
    // Tickets main page (protected)
    @GetMapping("/tickets")
    public String ticketsPage(Model model, @RequestParam(value = "activeTab", required = false) String activeTab) {
        model.addAttribute("ticket", new Ticket()); // For create form binding
        if (activeTab != null) {
            model.addAttribute("activeTab", activeTab);
        }
        
        try {
            // Get current logged in username
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Tickets page - Authentication: " + (auth != null ? auth.getName() : "null"));
            System.out.println("Tickets page - Is authenticated: " + (auth != null && auth.isAuthenticated()));
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                model.addAttribute("username", auth.getName());
            } else {
                model.addAttribute("username", "Guest");
                System.out.println("Warning: User not properly authenticated when accessing tickets page");
            }
        } catch (Exception e) {
            System.out.println("Error getting authentication in tickets page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("username", "Guest");
        }
        
        return "ticket"; // return to ticket.html
    }

    // Create Ticket
    @PostMapping("/tickets/create")
    public String createTicket(@ModelAttribute("ticket") Ticket ticket, Model model) {
        if (ticket.getPassengerName() == null || ticket.getEmail() == null) {
            model.addAttribute("error", "Passenger Name and Email are required!");
            model.addAttribute("activeTab", "create-tab");
            return "ticket";
        }
        
        // Get current logged in username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        
        ticketService.createTicket(ticket);
        model.addAttribute("success", "Ticket Created Successfully!");
        model.addAttribute("ticket", new Ticket()); // reset the form
        model.addAttribute("activeTab", "create-tab");
        return "ticket";
    }

    // List all tickets
    @GetMapping("/tickets/listall")
    public String listAllTickets(Model model) {
        List<Ticket> tickets = (List<Ticket>) ticketService.getAllTicket();
        model.addAttribute("tickets", tickets);
        model.addAttribute("ticket", new Ticket()); // for form binding
        model.addAttribute("activeTab", "list-tab");
        
        // Get current logged in username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        
        return "ticket";
    }

    // Get Ticket by ID
    @PostMapping("/tickets/get")
    public String getTicketById(@RequestParam("ticketId") Integer ticketId, Model model) {
        Ticket ticket = ticketService.getTicket(ticketId);
        if (ticket != null && ticket.getTicketId() != null) {
            model.addAttribute("foundTicket", ticket);
        } else {
            model.addAttribute("error", "Ticket not found with ID: " + ticketId);
        }
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("activeTab", "get-tab");
        
        // Get current logged in username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        
        return "ticket";
    }

    // Delete Ticket by ID
    @PostMapping("/tickets/delete")
    public String deleteTicketById(@RequestParam("ticketId") Integer ticketId, Model model) {
        ticketService.deleteTicket(ticketId);
        model.addAttribute("success", "Ticket deleted successfully!");
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("activeTab", "delete-tab");
        
        // Get current logged in username
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", auth.getName());
        
        return "ticket";
    }
}
