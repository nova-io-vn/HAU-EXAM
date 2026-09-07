package com.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="faculties")
public class FacultyEntity {
    @Id private UUID id;
    @Column(nullable=false, unique=true, length=50) private String code;
    @Column(nullable=false, length=255) private String name;
    @Column(length=1000) private String description;
    @Column(nullable=false) private boolean active;
    @Column(name="created_at", nullable=false, updatable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    public FacultyEntity() {}
    public UUID getId(){return id;} public void setId(UUID v){id=v;} public String getCode(){return code;} public void setCode(String v){code=v;}
    public String getName(){return name;} public void setName(String v){name=v;} public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public boolean isActive(){return active;} public void setActive(boolean v){active=v;} public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;} public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;}
}
