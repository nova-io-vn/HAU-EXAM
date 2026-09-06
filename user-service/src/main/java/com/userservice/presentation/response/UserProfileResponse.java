package com.userservice.presentation.response;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
public record UserProfileResponse(UUID id,String lecturerCode,String fullName,LocalDate dateOfBirth,Integer age,
                                  String phone,String email,String address,String avatar,String facultyId,Role role,
                                  UserStatus status,Instant createdAt,Instant updatedAt) { }
