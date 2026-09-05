package com.hau.user.application.dto;

import java.time.LocalDate;
import java.util.UUID;
public record RegistrationCommand(UUID eventId, UUID correlationId, UUID userId, String lecturerCode,
                                  String fullName, LocalDate dateOfBirth, String phone, String email,
                                  String address, String avatar, String facultyId) { }
