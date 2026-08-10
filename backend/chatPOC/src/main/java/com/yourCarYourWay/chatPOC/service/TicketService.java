package com.yourCarYourWay.chatPOC.service;

import com.yourCarYourWay.chatPOC.dto.SupportTicketDTO;
import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity;
import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity.TicketStatus;
import com.yourCarYourWay.chatPOC.entity.UserEntity;
import com.yourCarYourWay.chatPOC.repository.ChatMessageRepository;
import com.yourCarYourWay.chatPOC.repository.SupportTicketRepository;
import com.yourCarYourWay.chatPOC.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final SupportTicketRepository ticketRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public SupportTicketDTO createTicket(Long userId, String subject) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        SupportTicketEntity entity = SupportTicketEntity.builder()
                .subject(subject)
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        SupportTicketEntity saved = ticketRepository.save(entity);
        return mapToDTO(saved);
    }

    public SupportTicketDTO updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        SupportTicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketId));

        ticket.setStatus(newStatus);
        SupportTicketEntity saved = ticketRepository.save(ticket);
        return mapToDTO(saved);
    }

    @Transactional
    public void deleteTicket(Long ticketId) {
        chatMessageRepository.deleteBySupportTicketId(ticketId);
        ticketRepository.deleteById(ticketId);
    }

    public List<SupportTicketDTO> getTicketsForUser(Long userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<SupportTicketDTO> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public SupportTicketDTO getTicketById(Long id) {
        SupportTicketEntity ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found: " + id));
        return mapToDTO(ticket);
    }

    private SupportTicketDTO mapToDTO(SupportTicketEntity entity) {
        String name = entity.getUser() != null
                ? entity.getUser().getFirstName() + " " + entity.getUser().getLastName()
                : "Unknown User";

        return SupportTicketDTO.builder()
                .id(entity.getId())
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .userName(name)
                .build();
    }
}
