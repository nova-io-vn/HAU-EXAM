package com.authservice.infrastructure.messaging.contract;

import java.util.UUID;

public record UserSecuritySnapshotPayload(
        UUID userId,
        String lecturerCode,
        String role,
        String facultyId,
        String status,
        String email
) { }
