package com.userservice.application.service;

import com.userservice.application.dto.UserContact;
import com.userservice.application.port.in.UserContactQueryUseCase;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import com.userservice.domain.model.Role;

@Service
public class UserContactQueryService implements UserContactQueryUseCase {
    private final UserProfileRepository users;

    public UserContactQueryService(UserProfileRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserContact find(UUID userId) {
        var user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserContact(user.getId(), user.getLecturerCode(), user.getFullName(), user.getEmail(), user.getFacultyId());
    }

    @Transactional(readOnly = true)
    public List<ChatContact> contacts(UUID currentUserId, String role, String facultyId) {
        Role current = Role.valueOf(role);
        var result = new java.util.ArrayList<com.userservice.domain.model.UserProfile>();
        if (current == Role.SYSTEM_ADMIN) {
            result.addAll(users.findActiveAudience(Role.USER, null));
            result.addAll(users.findActiveAudience(Role.SUBJECT_ADMIN, null));
        } else {
            result.addAll(users.findActiveAudience(Role.SYSTEM_ADMIN, null));
        }
        return result.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .distinct()
                .map(user -> new ChatContact(user.getId(), user.getFullName(), user.getRole().name(), user.getFacultyId(), user.getAvatar()))
                .toList();
    }
    public record ChatContact(UUID userId, String displayName, String role, String facultyId, String avatarUrl) {}
}
