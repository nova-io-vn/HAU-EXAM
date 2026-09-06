package com.userservice.infrastructure.rabbitmq.contract;

import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import java.util.UUID;
public record UserChangedPayload(UUID userId,String lecturerCode,Role role,String facultyId,UserStatus status,
                                 String email, UUID recipientUserId) { }
