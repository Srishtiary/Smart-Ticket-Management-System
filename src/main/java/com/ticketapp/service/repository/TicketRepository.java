package com.ticketapp.service.repository;

import com.ticketapp.service.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TicketRepository extends MongoRepository<Ticket, String> {

    List<Ticket> findByCreatedBy(String userId);

    List<Ticket> findByAssignedTo(String userId);
}
