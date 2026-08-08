package com.yourCarYourWay.chatPOC.repository;

import com.yourCarYourWay.chatPOC.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findTop50ByOrderByTimestampAsc();
}
