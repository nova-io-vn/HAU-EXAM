package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.infrastructure.persistence.entity.SupportAttachmentEntity; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface SupportAttachmentRepository extends JpaRepository<SupportAttachmentEntity,UUID>{List<SupportAttachmentEntity> findByMessageId(UUID id);}
