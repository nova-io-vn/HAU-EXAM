package com.userservice.presentation.response;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import com.userservice.domain.model.AcademicDegree;
import com.userservice.domain.model.AcademicRank;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse(UUID id, String lecturerCode, String fullName, LocalDate dateOfBirth, Integer age,
                                  String phone, String email, String address, String avatar, String avatarPublicId, AcademicRank academicRank, AcademicDegree academicDegree, String facultyId,
                                  Role role, UserStatus status, Instant createdAt, Instant updatedAt) {
}
