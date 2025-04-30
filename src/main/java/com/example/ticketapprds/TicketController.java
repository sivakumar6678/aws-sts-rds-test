package com.example.ticketapprds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TicketController {
    
    @Autowired
    private TicketService ticketService;

    @PostMapping("/create")
    public ResponseEntity<Ticket> createTicket(@RequestBody Ticket ticketobj) {
        if (ticketobj.getPassengerName() == null || ticketobj.getEmail() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ticket createdTicket = ticketService.createTicket(ticketobj);
        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @GetMapping("/listall")
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = (List<Ticket>) ticketService.getAllTicket();
        return new ResponseEntity<>(tickets, HttpStatus.OK);
    }

    @GetMapping("/get/{ticketId}")
    public ResponseEntity<Ticket> getTicket(@PathVariable Integer ticketId) {
        // return ticketService.getTicket(ticketId);
        Ticket ticket = ticketService.getTicket(ticketId);
        return new ResponseEntity<>(ticket, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{ticketId}")
    public ResponseEntity<String> deleteTicket(@PathVariable Integer ticketId) {
        ticketService.deleteTicket(ticketId);
        return ResponseEntity.ok("Ticket deleted successfully");
    }
}