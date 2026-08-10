package com.yourCarYourWay.chatPOC.repository;

import com.yourCarYourWay.chatPOC.entity.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicketEntity, Long> {
    List<SupportTicketEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<SupportTicketEntity> findAllByOrderByCreatedAtDesc();
}
