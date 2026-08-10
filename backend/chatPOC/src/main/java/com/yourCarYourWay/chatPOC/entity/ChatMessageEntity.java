package com.yourCarYourWay.chatPOC.entity;

import com.yourCarYourWay.chatPOC.model.ChatMessage.MessageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender")
    private String sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "support_ticket_id")
    private SupportTicketEntity supportTicket;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.sender == null && this.user != null) {
            this.sender = this.user.getFirstName() + " " + this.user.getLastName();
        } else if (this.sender == null) {
            this.sender = "System";
        }
    }
}
