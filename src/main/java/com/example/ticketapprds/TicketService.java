package com.example.ticketapprds;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketService {
    
    @Autowired // Dependency injection
    private TicketDAO ticketdao;

    // Create Ticket
    public Ticket createTicket(Ticket ticketobj) {
        return ticketdao.save(ticketobj);
    }

    public Iterable<Ticket> getAllTicket() {
        return ticketdao.findAll();
    }

    public Ticket getTicket(Integer ticketId) {
        return ticketdao.findById(ticketId).orElse(new Ticket());
    }

    public void deleteTicket(Integer ticketId) {
        ticketdao.deleteById(ticketId);
    }
}