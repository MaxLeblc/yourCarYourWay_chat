package com.yourCarYourWay.chatPOC.dto;

import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicketDTO {
    private Long id;
    private String subject;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private Long userId;
    private String userName;
}
