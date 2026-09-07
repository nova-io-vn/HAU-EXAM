package com.userservice.infrastructure.rabbitmq.contract;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import java.util.UUID;

public record BootstrapAdminRequestedPayload(UUID userId, String lecturerCode, String email, String fullName,
                                             Role role, UserStatus status, String facultyId) { }
