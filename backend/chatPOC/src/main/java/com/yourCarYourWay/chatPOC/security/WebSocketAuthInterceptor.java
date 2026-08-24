package com.yourCarYourWay.chatPOC.security;

import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity;
import com.yourCarYourWay.chatPOC.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtils jwtUtils;
    private final SupportTicketRepository ticketRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String token = extractToken(accessor);

                if (token == null || !jwtUtils.validateToken(token)) {
                    log.warn("[WebSocket Security] Connection rejected: Invalid or missing JWT token.");
                    throw new IllegalArgumentException("Unauthorized: Invalid or missing JWT token.");
                }

                String email = jwtUtils.getEmailFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                Long userId = jwtUtils.getUserIdFromToken(token);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        email,
                        userId,
                        role != null ? List.of(new SimpleGrantedAuthority(role)) : List.of()
                );

                accessor.setUser(auth);
                log.info("[WebSocket Security] Authenticated WebSocket user: {} (Role: {}, UserId: {})", email, role, userId);

            } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                String destination = accessor.getDestination();
                Principal principal = accessor.getUser();

                if (principal == null) {
                    log.warn("[WebSocket Security] Subscription rejected: Unauthenticated session.");
                    throw new IllegalArgumentException("Unauthorized: Session is not authenticated.");
                }

                if (destination != null && destination.startsWith("/topic/ticket/")) {
                    try {
                        Long ticketId = Long.parseLong(destination.substring("/topic/ticket/".length()));
                        validateTicketAccess(ticketId, principal);
                    } catch (NumberFormatException e) {
                        log.warn("[WebSocket Security] Invalid ticket destination format: {}", destination);
                    }
                }
            }
        }

        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String tokenHeader = accessor.getFirstNativeHeader("token");
        if (tokenHeader != null && !tokenHeader.isEmpty()) {
            return tokenHeader;
        }
        return null;
    }

    private void validateTicketAccess(Long ticketId, Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken authToken) {
            boolean isStaff = authToken.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_AGENCY_STAFF".equals(a.getAuthority()));

            if (isStaff) {
                // Support agents have access to all tickets
                return;
            }

            Long userId = (Long) authToken.getCredentials();
            SupportTicketEntity ticket = ticketRepository.findById(ticketId).orElse(null);

            if (ticket != null && ticket.getUser() != null) {
                if (!ticket.getUser().getId().equals(userId)) {
                    log.warn("[WebSocket Security] Access denied: User {} tried to access Ticket #{} owned by User {}",
                            userId, ticketId, ticket.getUser().getId());
                    throw new IllegalArgumentException("Access Denied: You do not own this ticket.");
                }
            }
        }
    }
}
