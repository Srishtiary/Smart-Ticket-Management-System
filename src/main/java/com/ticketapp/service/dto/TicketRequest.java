package com.ticketapp.service.dto;

import com.ticketapp.service.model.TicketPriority;
import lombok.Data;

@Data
public class TicketRequest {
    private String title;
    private String description;
    private TicketPriority priority;
}
