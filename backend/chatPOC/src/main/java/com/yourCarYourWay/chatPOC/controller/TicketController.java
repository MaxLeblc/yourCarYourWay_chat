package com.yourCarYourWay.chatPOC.controller;

import com.yourCarYourWay.chatPOC.dto.SupportTicketDTO;
import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity.TicketStatus;
import com.yourCarYourWay.chatPOC.model.ChatMessage;
import com.yourCarYourWay.chatPOC.service.ChatService;
import com.yourCarYourWay.chatPOC.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<SupportTicketDTO> createTicket(@RequestBody Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());
        String subject = payload.get("subject").toString();
        return ResponseEntity.ok(ticketService.createTicket(userId, subject));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<SupportTicketDTO> updateStatus(@PathVariable Long id, @RequestParam TicketStatus status) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SupportTicketDTO>> getTicketsForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ticketService.getTicketsForUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<SupportTicketDTO>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportTicketDTO> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ChatMessage>> getTicketHistory(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getHistoryForTicket(id));
    }
}
