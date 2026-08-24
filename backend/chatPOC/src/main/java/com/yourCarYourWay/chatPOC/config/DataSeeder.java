package com.yourCarYourWay.chatPOC.config;

import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity;
import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity.TicketStatus;
import com.yourCarYourWay.chatPOC.entity.UserEntity;
import com.yourCarYourWay.chatPOC.repository.SupportTicketRepository;
import com.yourCarYourWay.chatPOC.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SupportTicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Ensure default seeded customer exists with Password123
        UserEntity client = userRepository.findByEmail("client@gmail.com").orElse(null);
        if (client == null) {
            client = UserEntity.builder()
                    .firstName("Alex")
                    .lastName("Dupont")
                    .email("client@gmail.com")
                    .password(passwordEncoder.encode("Password123"))
                    .phoneNumber("+33612345678")
                    .role("ROLE_USER")
                    .build();
            client = userRepository.save(client);
        } else {
            client.setPassword(passwordEncoder.encode("Password123"));
            client = userRepository.save(client);
        }

        // Ensure default seeded agent exists with Password123
        UserEntity agent = userRepository.findByEmail("agent@ycyw.com").orElse(null);
        if (agent == null) {
            agent = UserEntity.builder()
                    .firstName("Sarah")
                    .lastName("Martin")
                    .email("agent@ycyw.com")
                    .password(passwordEncoder.encode("Password123"))
                    .phoneNumber("+33687654321")
                    .role("ROLE_AGENCY_STAFF")
                    .build();
            userRepository.save(agent);
        } else {
            agent.setPassword(passwordEncoder.encode("Password123"));
            userRepository.save(agent);
        }

        // Ensure at least one initial ticket exists
        if (ticketRepository.count() == 0) {
            SupportTicketEntity ticket = SupportTicketEntity.builder()
                    .subject("Inquiry regarding Booking #102")
                    .status(TicketStatus.OPEN)
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .user(client)
                    .build();
            ticketRepository.save(ticket);
        }
    }
}
