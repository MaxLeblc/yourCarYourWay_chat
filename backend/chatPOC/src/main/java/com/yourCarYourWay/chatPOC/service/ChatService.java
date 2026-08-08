package com.yourCarYourWay.chatPOC.service;

import com.yourCarYourWay.chatPOC.entity.ChatMessageEntity;
import com.yourCarYourWay.chatPOC.model.ChatMessage;
import com.yourCarYourWay.chatPOC.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveMessage(ChatMessage chatMessage) {
        ChatMessageEntity entity = ChatMessageEntity.builder()
                .sender(chatMessage.getSender())
                .userId(chatMessage.getUserId())
                .supportTicketId(chatMessage.getSupportTicketId())
                .content(chatMessage.getContent())
                .type(chatMessage.getType())
                .timestamp(chatMessage.getTimestamp())
                .build();

        chatMessageRepository.save(entity);
        return chatMessage;
    }

    public List<ChatMessage> getHistory() {
        return chatMessageRepository.findTop50ByOrderByTimestampAsc().stream()
                .map(entity -> ChatMessage.builder()
                        .sender(entity.getSender())
                        .userId(entity.getUserId())
                        .supportTicketId(entity.getSupportTicketId())
                        .content(entity.getContent())
                        .type(entity.getType())
                        .timestamp(entity.getTimestamp())
                        .build())
                .toList();
    }
}
