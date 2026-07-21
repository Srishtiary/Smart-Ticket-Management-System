package com.ticketapp.service.service;

import com.ticketapp.service.dto.TicketRequest;
import com.ticketapp.service.dto.TicketResponse;
import com.ticketapp.service.exception.AccessDeniedCustomException;
import com.ticketapp.service.exception.ResourceNotFoundException;
import com.ticketapp.service.model.Role;
import com.ticketapp.service.model.Ticket;
import com.ticketapp.service.model.TicketPriority;
import com.ticketapp.service.model.TicketStatus;
import com.ticketapp.service.model.User;
import com.ticketapp.service.repository.TicketRepository;
import com.ticketapp.service.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    /**
     * Helper method to get the currently authenticated user from the SecurityContext
     */
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database"));
    }

    /**
     * Helper method to map a Ticket entity to a TicketResponse DTO
     */
    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .createdBy(ticket.getCreatedBy())
                .assignedTo(ticket.getAssignedTo())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public TicketResponse createTicket(TicketRequest request) {
        User currentUser = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM)
                .status(TicketStatus.OPEN)
                .createdBy(currentUser.getId())
                .createdAt(now)
                .updatedAt(now)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToResponse(savedTicket);
    }

    public List<TicketResponse> getMyTickets() {
        User currentUser = getCurrentUser();

        List<Ticket> tickets;
        if (currentUser.getRole() == Role.ADMIN) {
            tickets = ticketRepository.findAll();
        } else {
            tickets = ticketRepository.findByCreatedBy(currentUser.getId());
        }

        return tickets.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TicketResponse getTicketById(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser();

        // If the user is a standard USER, they can only view their own tickets
        if (currentUser.getRole() == Role.USER && !currentUser.getId().equals(ticket.getCreatedBy())) {
            throw new AccessDeniedCustomException("You do not have permission to view this ticket");
        }

        return mapToResponse(ticket);
    }

    public TicketResponse updateTicketStatus(String id, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User currentUser = getCurrentUser();

        // Only an ADMIN or the explicitly assigned user can update the status
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        boolean isAssignedUser = currentUser.getId().equals(ticket.getAssignedTo());

        if (!isAdmin && !isAssignedUser) {
            throw new AccessDeniedCustomException("Only an admin or the assigned user can update the ticket status");
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToResponse(updatedTicket);
    }

    public TicketResponse assignTicket(String id, String assignToUserId) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("Only an admin can assign tickets");
        }

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        // Verify the user being assigned actually exists
        if (!userRepository.existsById(assignToUserId)) {
            throw new ResourceNotFoundException("Target assignment user does not exist");
        }

        ticket.setAssignedTo(assignToUserId);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToResponse(updatedTicket);
    }

    public void deleteTicket(String id) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedCustomException("Only an admin can delete tickets");
        }

        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket not found");
        }

        ticketRepository.deleteById(id);
    }
}
