package com.hau.user.infrastructure.rabbitmq.contract;

import java.time.LocalDate;
import java.util.UUID;
public record RegistrationRequestedPayload(UUID userId,String lecturerCode,String fullName,LocalDate dateOfBirth,
                                           String phone,String email,String address,String avatar,String facultyId) { }
