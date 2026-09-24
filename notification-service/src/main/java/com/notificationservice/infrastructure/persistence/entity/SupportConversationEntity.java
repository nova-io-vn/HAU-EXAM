package com.notificationservice.infrastructure.persistence.entity;
import com.notificationservice.domain.model.SupportStatus;
import jakarta.persistence.*; import java.time.Instant; import java.util.UUID;
@Entity @Table(name="support_conversations")
public class SupportConversationEntity {
 @Id private UUID id; @Column(name="created_by_user_id",nullable=false) private UUID createdByUserId;
 @Column(name="created_by_role",nullable=false,length=32) private String createdByRole; @Column(name="faculty_id",length=80) private String facultyId;
 @Column(nullable=false,length=200) private String subject; @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private SupportStatus status;
 @Column(name="assigned_admin_id") private UUID assignedAdminId; @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(name="updated_at",nullable=false) private Instant updatedAt; @Column(name="last_message_at",nullable=false) private Instant lastMessageAt;
 public UUID getId(){return id;} public void setId(UUID v){id=v;} public UUID getCreatedByUserId(){return createdByUserId;} public void setCreatedByUserId(UUID v){createdByUserId=v;}
 public String getCreatedByRole(){return createdByRole;} public void setCreatedByRole(String v){createdByRole=v;} public String getFacultyId(){return facultyId;} public void setFacultyId(String v){facultyId=v;}
 public String getSubject(){return subject;} public void setSubject(String v){subject=v;} public SupportStatus getStatus(){return status;} public void setStatus(SupportStatus v){status=v;}
 public UUID getAssignedAdminId(){return assignedAdminId;} public void setAssignedAdminId(UUID v){assignedAdminId=v;} public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
 public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;} public Instant getLastMessageAt(){return lastMessageAt;} public void setLastMessageAt(Instant v){lastMessageAt=v;}
}
