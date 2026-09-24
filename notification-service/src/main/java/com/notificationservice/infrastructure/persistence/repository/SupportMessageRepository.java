package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.SupportMessageEntity; import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SupportMessageRepository extends JpaRepository<SupportMessageEntity,UUID>{Page<SupportMessageEntity> findByConversationIdOrderByCreatedAtDesc(UUID id,Pageable p);long countBySenderRoleAndReadAtIsNull(String senderRole);long countBySenderRoleNotAndReadAtIsNull(String senderRole);}
