package com.hau.user.infrastructure.rabbitmq.contract;

import com.hau.user.domain.model.Role;
import com.hau.user.domain.model.UserStatus;
import java.util.UUID;
public record UserChangedPayload(UUID userId,String lecturerCode,Role role,String facultyId,UserStatus status) { }
