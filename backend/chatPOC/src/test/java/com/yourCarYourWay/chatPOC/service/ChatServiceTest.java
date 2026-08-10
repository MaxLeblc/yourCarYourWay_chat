package com.yourCarYourWay.chatPOC.service;

import com.yourCarYourWay.chatPOC.entity.ChatMessageEntity;
import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity;
import com.yourCarYourWay.chatPOC.entity.UserEntity;
import com.yourCarYourWay.chatPOC.model.ChatMessage;
import com.yourCarYourWay.chatPOC.model.ChatMessage.MessageType;
import com.yourCarYourWay.chatPOC.repository.ChatMessageRepository;
import com.yourCarYourWay.chatPOC.repository.SupportTicketRepository;
import com.yourCarYourWay.chatPOC.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SupportTicketRepository ticketRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void testSaveMessage() {
        UserEntity user = UserEntity.builder().id(1L).firstName("Alex").lastName("Dupont").email("client@gmail.com").role("ROLE_USER").build();
        SupportTicketEntity ticket = SupportTicketEntity.builder().id(10L).subject("Test Ticket").user(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(chatMessageRepository.save(any(ChatMessageEntity.class))).thenAnswer(i -> i.getArgument(0));

        ChatMessage message = ChatMessage.builder()
                .userId(1L)
                .supportTicketId(10L)
                .content("Hello Agent")
                .type(MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatService.saveMessage(message);

        assertNotNull(saved);
        assertEquals("Alex Dupont", saved.getSender());
        verify(chatMessageRepository, times(1)).save(any(ChatMessageEntity.class));
    }

    @Test
    void testGetHistoryForTicket() {
        UserEntity user = UserEntity.builder().id(1L).firstName("Alex").lastName("Dupont").role("ROLE_USER").build();
        SupportTicketEntity ticket = SupportTicketEntity.builder().id(10L).subject("Test Ticket").user(user).build();

        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(100L)
                .content("Hello Agent")
                .type(MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .user(user)
                .supportTicket(ticket)
                .build();

        when(chatMessageRepository.findBySupportTicketIdOrderByTimestampAsc(10L)).thenReturn(List.of(entity));

        List<ChatMessage> history = chatService.getHistoryForTicket(10L);

        assertEquals(1, history.size());
        assertEquals("Hello Agent", history.get(0).getContent());
        assertEquals("Alex Dupont", history.get(0).getSender());
    }
}
