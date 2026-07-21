package com.ticketapp.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Represents a support ticket in the system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tickets")
public class Ticket {

    @Id
    private String id;

    private String title;

    private String description;

    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    private String createdBy;

    private String assignedTo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
