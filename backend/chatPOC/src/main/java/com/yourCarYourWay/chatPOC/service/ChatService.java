package com.yourCarYourWay.chatPOC.service;

import com.yourCarYourWay.chatPOC.entity.ChatMessageEntity;
import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity;
import com.yourCarYourWay.chatPOC.entity.UserEntity;
import com.yourCarYourWay.chatPOC.model.ChatMessage;
import com.yourCarYourWay.chatPOC.model.ChatMessage.MessageType;
import com.yourCarYourWay.chatPOC.repository.ChatMessageRepository;
import com.yourCarYourWay.chatPOC.repository.SupportTicketRepository;
import com.yourCarYourWay.chatPOC.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SupportTicketRepository ticketRepository;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        UserEntity user = null;
        if (chatMessage.getUserId() != null) {
            user = userRepository.findById(chatMessage.getUserId()).orElse(null);
        }
        if (user == null && chatMessage.getSender() != null) {
            user = userRepository.findByEmail(chatMessage.getSender()).orElse(null);
        }

        SupportTicketEntity ticket = null;
        if (chatMessage.getSupportTicketId() != null) {
            ticket = ticketRepository.findById(chatMessage.getSupportTicketId()).orElse(null);
        }

        String senderName = chatMessage.getSender();
        if (senderName == null && user != null) {
            senderName = user.getFirstName() + " " + user.getLastName();
        }

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .content(chatMessage.getContent())
                .sender(senderName)
                .type(chatMessage.getType() != null ? chatMessage.getType() : MessageType.CHAT)
                .timestamp(chatMessage.getTimestamp() != null ? chatMessage.getTimestamp() : LocalDateTime.now())
                .user(user)
                .supportTicket(ticket)
                .build();

        try {
            ChatMessageEntity saved = chatMessageRepository.save(entity);
            return mapToDTO(saved, chatMessage.getSender());
        } catch (Exception e) {
            System.err.println("Error saving chat message to DB: " + e.getMessage());
            return chatMessage;
        }
    }

    public List<ChatMessage> getHistoryForTicket(Long supportTicketId) {
        return chatMessageRepository.findBySupportTicketIdOrderByTimestampAsc(supportTicketId).stream()
                .map(entity -> mapToDTO(entity, null))
                .toList();
    }

    public List<ChatMessage> getHistory() {
        return chatMessageRepository.findAll().stream()
                .map(entity -> mapToDTO(entity, null))
                .toList();
    }

    private ChatMessage mapToDTO(ChatMessageEntity entity, String fallbackSender) {
        String senderName = entity.getSender() != null ? entity.getSender() : fallbackSender;

        if (entity.getUser() != null && entity.getUser().getFirstName() != null) {
            senderName = entity.getUser().getFirstName() + " " + (entity.getUser().getLastName() != null ? entity.getUser().getLastName() : "");
        }
        if (senderName == null) {
            senderName = "System";
        }

        return ChatMessage.builder()
                .id(entity.getId())
                .sender(senderName.trim())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .supportTicketId(entity.getSupportTicket() != null ? entity.getSupportTicket().getId() : null)
                .content(entity.getContent())
                .type(entity.getType())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
