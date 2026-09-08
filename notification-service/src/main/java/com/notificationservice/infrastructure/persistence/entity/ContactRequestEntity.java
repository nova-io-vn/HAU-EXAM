package com.notificationservice.infrastructure.persistence.entity;

import com.notificationservice.domain.model.ContactStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="contact_requests")
public class ContactRequestEntity {
    @Id private UUID id;
    @Column(nullable=false,length=160) private String name;
    @Column(nullable=false,length=254) private String email;
    @Column(nullable=false,length=200) private String subject;
    @Column(nullable=false,length=5000) private String message;
    @Column(name="faculty_id",length=80) private String facultyId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=24) private ContactStatus status;
    @Column(name="admin_note",length=5000) private String adminNote;
    @Column(name="reply_message",length=5000) private String replyMessage;
    @Column(name="created_at",nullable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Column(name="replied_at") private Instant repliedAt;
    public ContactRequestEntity(){}
    public UUID getId(){return id;} public void setId(UUID v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getSubject(){return subject;} public void setSubject(String v){subject=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getFacultyId(){return facultyId;} public void setFacultyId(String v){facultyId=v;}
    public ContactStatus getStatus(){return status;} public void setStatus(ContactStatus v){status=v;}
    public String getAdminNote(){return adminNote;} public void setAdminNote(String v){adminNote=v;}
    public String getReplyMessage(){return replyMessage;} public void setReplyMessage(String v){replyMessage=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
    public Instant getRepliedAt(){return repliedAt;} public void setRepliedAt(Instant v){repliedAt=v;}
}
