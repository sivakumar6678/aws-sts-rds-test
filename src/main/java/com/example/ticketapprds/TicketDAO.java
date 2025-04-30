package com.example.ticketapprds;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketDAO extends CrudRepository<Ticket,Integer>{

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
