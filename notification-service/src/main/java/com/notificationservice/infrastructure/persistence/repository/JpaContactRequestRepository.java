package com.notificationservice.infrastructure.persistence.repository;
import com.notificationservice.domain.model.ContactStatus;
import com.notificationservice.infrastructure.persistence.entity.ContactRequestEntity;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface JpaContactRequestRepository extends JpaRepository<ContactRequestEntity,UUID>{Page<ContactRequestEntity> findByStatus(ContactStatus status,Pageable pageable);}
