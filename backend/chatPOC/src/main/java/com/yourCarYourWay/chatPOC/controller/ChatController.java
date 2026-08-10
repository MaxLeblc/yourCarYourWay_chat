package com.yourCarYourWay.chatPOC.controller;

import com.yourCarYourWay.chatPOC.model.ChatMessage;
import com.yourCarYourWay.chatPOC.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessage/{ticketId}")
    public void sendMessage(@DestinationVariable("ticketId") Long ticketId, @Payload ChatMessage chatMessage) {
        chatMessage.setSupportTicketId(ticketId);
        ChatMessage saved = chatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend("/topic/ticket/" + ticketId, saved);
    }
}
