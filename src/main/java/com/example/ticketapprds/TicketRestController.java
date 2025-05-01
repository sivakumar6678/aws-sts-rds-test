package com.example.ticketapprds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TicketRestController {

    @Autowired
    private TicketService ticketService;

    // Get all tickets
    @GetMapping("/tickets")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = (List<Ticket>) ticketService.getAllTicket();
        return ResponseEntity.ok(tickets);
    }

    // Get ticket by ID
    @GetMapping("/tickets/{id}")
    public ResponseEntity<?> getTicketById(@PathVariable("id") Integer ticketId) {
        Ticket ticket = ticketService.getTicket(ticketId);
        if (ticket != null) {
            return ResponseEntity.ok(ticket);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Ticket not found with ID: " + ticketId);
            return ResponseEntity.status(404).body(response);
        }
    }

    // Create ticket
    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(@RequestBody Ticket ticket) {
        if (ticket.getPassengerName() == null || ticket.getEmail() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Passenger Name and Email are required!");
            return ResponseEntity.badRequest().body(response);
        }
        Ticket createdTicket = ticketService.createTicket(ticket);
        return ResponseEntity.ok(createdTicket);
    }

    // Delete ticket
    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<?> deleteTicket(@PathVariable("id") Integer ticketId) {
        try {
            ticketService.deleteTicket(ticketId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ticket deleted successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to delete ticket: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}