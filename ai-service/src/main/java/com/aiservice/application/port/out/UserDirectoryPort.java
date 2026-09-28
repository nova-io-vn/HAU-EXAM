package com.aiservice.application.port.out;

import java.util.*;

public interface UserDirectoryPort {
    Optional<UserProfile> find(UUID userId);
    Map<UUID, UserProfile> findAll(Set<UUID> userIds);
    default String displayName(UUID userId) { return find(userId).map(UserProfile::fullName).orElse(null); }
    record UserProfile(UUID userId, String lecturerCode, String fullName, String email, String facultyId,
                       String role, String status, String academicRank, String academicDegree, String avatarUrl) { }
}
