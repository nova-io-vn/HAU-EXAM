package com.hau.user.domain.model;

import com.hau.user.domain.exception.InvalidStatusTransitionException;
import com.hau.user.domain.exception.InvalidUserProfileException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class UserProfile {
    private final UUID id;
    private final String lecturerCode;
    private final String fullName;
    private final LocalDate dateOfBirth;
    private final String phone;
    private final String email;
    private final String address;
    private final String avatar;
    private final String facultyId;
    private final Role role;
    private final UserStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final long version;

    public UserProfile(UUID id, String lecturerCode, String fullName, LocalDate dateOfBirth,
                       String phone, String email, String address, String avatar, String facultyId,
                       Role role, UserStatus status, Instant createdAt, Instant updatedAt, long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.lecturerCode = required(lecturerCode, "lecturerCode", 50).toUpperCase(Locale.ROOT);
        this.fullName = required(fullName, "fullName", 150);
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) throw new InvalidUserProfileException("dateOfBirth must not be in the future");
        this.dateOfBirth = dateOfBirth;
        this.phone = optional(phone, 20, "phone");
        this.email = required(email, "email", 254).toLowerCase(Locale.ROOT);
        this.address = optional(address, 500, "address");
        this.avatar = optional(avatar, 1000, "avatar");
        this.facultyId = optional(facultyId, 50, "facultyId");
        this.role = Objects.requireNonNull(role, "role must not be null");
        if (role == Role.SUBJECT_ADMIN && this.facultyId == null) throw new InvalidUserProfileException("SUBJECT_ADMIN must have a facultyId");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        if (updatedAt.isBefore(createdAt)) throw new InvalidUserProfileException("updatedAt must not be before createdAt");
        if (version < 0) throw new InvalidUserProfileException("version must not be negative");
        this.version = version;
    }

    public static UserProfile pending(UUID id, String lecturerCode, String fullName, LocalDate dateOfBirth,
                                      String phone, String email, String address, String avatar, String facultyId, Instant now) {
        return new UserProfile(id, lecturerCode, fullName, dateOfBirth, phone, email, address, avatar, facultyId,
                Role.USER, UserStatus.PENDING_APPROVAL, now, now, 0);
    }
    public UserProfile updateProfile(String fullName, LocalDate dob, String phone, String email, String address, String avatar, Instant at) {
        return new UserProfile(id, lecturerCode, fullName, dob, phone, email, address, avatar, facultyId, role, status, createdAt, at, version);
    }
    public UserProfile approve(Instant at) {
        if (status != UserStatus.PENDING_APPROVAL) throw new InvalidStatusTransitionException("Only pending users can be approved");
        return copy(role, UserStatus.ACTIVE, facultyId, at);
    }
    public UserProfile reject(Instant at) {
        if (status != UserStatus.PENDING_APPROVAL) throw new InvalidStatusTransitionException("Only pending users can be rejected");
        return copy(role, UserStatus.REJECTED, facultyId, at);
    }
    public UserProfile lock(Instant at) {
        if (status != UserStatus.ACTIVE) throw new InvalidStatusTransitionException("Only active users can be locked");
        return copy(role, UserStatus.LOCKED, facultyId, at);
    }
    public UserProfile unlock(Instant at) {
        if (status != UserStatus.LOCKED) throw new InvalidStatusTransitionException("Only locked users can be unlocked");
        return copy(role, UserStatus.ACTIVE, facultyId, at);
    }
    public UserProfile assignRole(Role role, Instant at) { return copy(Objects.requireNonNull(role), status, facultyId, at); }
    public UserProfile assignFaculty(String facultyId, Instant at) { return copy(role, status, facultyId, at); }
    public int age(Clock clock) { return dateOfBirth == null ? 0 : Period.between(dateOfBirth, LocalDate.now(clock)).getYears(); }
    private UserProfile copy(Role newRole, UserStatus newStatus, String newFaculty, Instant at) {
        return new UserProfile(id, lecturerCode, fullName, dateOfBirth, phone, email, address, avatar,
                newFaculty, newRole, newStatus, createdAt, at, version);
    }
    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank()) throw new InvalidUserProfileException(field + " must not be blank");
        String result = value.trim(); if (result.length() > max) throw new InvalidUserProfileException(field + " is too long"); return result;
    }
    private static String optional(String value, int max, String field) {
        if (value == null || value.isBlank()) return null; String result = value.trim();
        if (result.length() > max) throw new InvalidUserProfileException(field + " is too long"); return result;
    }
    public UUID getId() { return id; } public String getLecturerCode() { return lecturerCode; }
    public String getFullName() { return fullName; } public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; } public String getEmail() { return email; }
    public String getAddress() { return address; } public String getAvatar() { return avatar; }
    public String getFacultyId() { return facultyId; } public Role getRole() { return role; }
    public UserStatus getStatus() { return status; } public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public long getVersion() { return version; }
}
