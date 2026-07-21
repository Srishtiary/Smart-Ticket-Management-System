package com.ticketapp.service.repository;

import com.ticketapp.service.model.Ticket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Repository for managing Ticket documents.
 */
public interface TicketRepository extends MongoRepository<Ticket, String> {

    /**
     * Finds tickets created by a specific user.
     */
    List<Ticket> findByCreatedBy(String userId);

    /**
     * Finds tickets assigned to a specific user.
     */
    List<Ticket> findByAssignedTo(String userId);
}
