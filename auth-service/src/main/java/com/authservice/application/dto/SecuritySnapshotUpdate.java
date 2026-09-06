package com.authservice.application.dto;

import java.util.UUID;

public record SecuritySnapshotUpdate(
        UUID eventId,
        String eventType,
        int eventVersion,
        UUID userId,
        String lecturerCode,
        String status,
        String role,
        String facultyId,
        String email
) { }
