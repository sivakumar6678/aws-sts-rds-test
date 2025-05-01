package com.example.ticketapprds;

import org.springframework.beans.factory.annotation.Autowired;
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
        model.addAttribute("ticket", new Ticket()); // For create form binding
        if (activeTab != null) {
            model.addAttribute("activeTab", activeTab);
        }
        return "ticket"; // return to ticket.html
    }

    // Create Ticket
    @PostMapping("/create")
    public String createTicket(@ModelAttribute("ticket") Ticket ticket, Model model) {
        if (ticket.getPassengerName() == null || ticket.getEmail() == null) {
            model.addAttribute("error", "Passenger Name and Email are required!");
            model.addAttribute("activeTab", "create-tab");
            return "ticket";
        }
        ticketService.createTicket(ticket);
        model.addAttribute("success", "Ticket Created Successfully!");
        model.addAttribute("ticket", new Ticket()); // reset the form
        model.addAttribute("activeTab", "create-tab");
        return "ticket";
    }

    // List all tickets
    @GetMapping("/listall")
    public String listAllTickets(Model model) {
        List<Ticket> tickets = (List<Ticket>) ticketService.getAllTicket();
        model.addAttribute("tickets", tickets);
        model.addAttribute("ticket", new Ticket()); // for form binding
        model.addAttribute("activeTab", "list-tab");
        return "ticket";
    }

    // Get Ticket by ID
    @PostMapping("/get")
    public String getTicketById(@RequestParam("ticketId") Integer ticketId, Model model) {
        Ticket ticket = ticketService.getTicket(ticketId);
        if (ticket != null && ticket.getTicketId() != null) {
            model.addAttribute("foundTicket", ticket);
        } else {
            model.addAttribute("error", "Ticket not found with ID: " + ticketId);
        }
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("activeTab", "get-tab");
        return "ticket";
    }

    // Delete Ticket by ID
    @PostMapping("/delete")
    public String deleteTicketById(@RequestParam("ticketId") Integer ticketId, Model model) {
        ticketService.deleteTicket(ticketId);
        model.addAttribute("success", "Ticket deleted successfully!");
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("activeTab", "delete-tab");
        return "ticket";
    }
}
