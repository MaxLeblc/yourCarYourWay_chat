package com.yourCarYourWay.chatPOC.service;

import com.yourCarYourWay.chatPOC.entity.ChatMessageEntity;
import com.yourCarYourWay.chatPOC.model.ChatMessage;
import com.yourCarYourWay.chatPOC.model.ChatMessage.MessageType;
import com.yourCarYourWay.chatPOC.repository.ChatMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private ChatService chatService;

    @Test
    void testSaveMessage() {
        ChatMessage message = ChatMessage.builder()
                .sender("User1")
                .content("Hello World")
                .type(MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage saved = chatService.saveMessage(message);

        assertNotNull(saved);
        assertEquals("User1", saved.getSender());
        verify(chatMessageRepository, times(1)).save(any(ChatMessageEntity.class));
    }

    @Test
    void testGetHistory() {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .id(1L)
                .sender("User1")
                .content("Hello")
                .type(MessageType.CHAT)
                .timestamp(LocalDateTime.now())
                .build();

        when(chatMessageRepository.findTop50ByOrderByTimestampAsc()).thenReturn(List.of(entity));

        List<ChatMessage> history = chatService.getHistory();

        assertEquals(1, history.size());
        assertEquals("Hello", history.get(0).getContent());
    }
}
