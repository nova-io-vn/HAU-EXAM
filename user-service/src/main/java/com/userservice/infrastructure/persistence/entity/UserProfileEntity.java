package com.userservice.infrastructure.persistence.entity;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity @Table(name="user_profiles")
public class UserProfileEntity {
    @Id private UUID id;
    @Column(name="lecturer_code",nullable=false,unique=true,length=50) private String lecturerCode;
    @Column(name="full_name",nullable=false,length=150) private String fullName;
    @Column(name="date_of_birth") private LocalDate dateOfBirth;
    @Column(length=20) private String phone;
    @Column(nullable=false,unique=true,length=254) private String email;
    @Column(length=500) private String address;
    @Column(length=1000) private String avatar;
    @Column(name="faculty_id",length=50) private String facultyId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private Role role;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private UserStatus status;
    @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt;
    @Column(name="updated_at",nullable=false) private Instant updatedAt;
    @Version @Column(nullable=false) private long version;
    public UserProfileEntity() { }
    public UUID getId(){return id;} public void setId(UUID v){id=v;} public String getLecturerCode(){return lecturerCode;} public void setLecturerCode(String v){lecturerCode=v;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;} public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate v){dateOfBirth=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;} public String getAvatar(){return avatar;} public void setAvatar(String v){avatar=v;}
    public String getFacultyId(){return facultyId;} public void setFacultyId(String v){facultyId=v;} public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public UserStatus getStatus(){return status;} public void setStatus(UserStatus v){status=v;} public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant v){updatedAt=v;} public long getVersion(){return version;} public void setVersion(long v){version=v;}
}
