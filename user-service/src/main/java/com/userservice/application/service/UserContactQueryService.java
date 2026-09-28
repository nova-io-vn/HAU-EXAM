package com.userservice.application.service;

import com.userservice.application.dto.UserContact;
import com.userservice.application.port.in.UserContactQueryUseCase;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Objects;
import com.userservice.domain.model.Role;
import com.userservice.domain.exception.UserNotFoundException;

@Service
public class UserContactQueryService implements UserContactQueryUseCase {
    private final UserProfileRepository users;

    public UserContactQueryService(UserProfileRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserContact find(UUID userId) {
        var user = users.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return contact(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserContact> findAll(Set<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return users.findAllById(userIds).stream().map(this::contact).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserContact> findActiveLecturers(Set<String> facultyIds, String keyword) {
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Set<String> requestedScopes = facultyIds == null ? Set.of() : facultyIds.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .collect(java.util.stream.Collectors.toSet());
        var lecturers = requestedScopes.isEmpty()
                ? users.findActiveAudience(Role.USER, null)
                : requestedScopes.stream().flatMap(facultyId -> users.findActiveAudience(Role.USER, facultyId).stream()).toList();
        return lecturers.stream()
                .filter(user -> query.isEmpty() || (user.getFullName() + " " + user.getLecturerCode()).toLowerCase(Locale.ROOT).contains(query))
                .distinct()
                .map(this::contact)
                .toList();
    }

    private UserContact contact(com.userservice.domain.model.UserProfile user) {
        return new UserContact(user.getId(), user.getLecturerCode(), user.getFullName(), user.getEmail(), user.getFacultyId(),
                user.getRole().name(), user.getStatus().name(), user.getAcademicRank().name(), user.getAcademicDegree().name(), user.getAvatar());
    }

    @Transactional(readOnly = true)
    public List<ChatContact> contacts(UUID currentUserId, String role, String facultyId) {
        return contacts(currentUserId, role, facultyId, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<ChatContact> contacts(UUID currentUserId, String role, String facultyId,
                                      String keyword, String requestedFaculty, String requestedRole) {
        Role current = Role.valueOf(role);
        var result = new java.util.ArrayList<com.userservice.domain.model.UserProfile>();
        if (current == Role.SYSTEM_ADMIN) {
            result.addAll(users.findActiveAudience(Role.USER, null));
            result.addAll(users.findActiveAudience(Role.SUBJECT_ADMIN, null));
        } else if (current == Role.SUBJECT_ADMIN) {
            result.addAll(users.findActiveAudience(Role.USER, facultyId));
            result.addAll(users.findActiveAudience(Role.SYSTEM_ADMIN, null));
        } else {
            result.addAll(users.findActiveAudience(Role.SUBJECT_ADMIN, facultyId));
            result.addAll(users.findActiveAudience(Role.SYSTEM_ADMIN, null));
        }
        String query = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        Role roleFilter = requestedRole == null || requestedRole.isBlank() ? null : Role.valueOf(requestedRole);
        String facultyFilter = requestedFaculty == null || requestedFaculty.isBlank() ? null : requestedFaculty.trim();
        return result.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .filter(user -> roleFilter == null || user.getRole() == roleFilter)
                .filter(user -> facultyFilter == null || Objects.equals(facultyFilter, user.getFacultyId()))
                .filter(user -> query.isEmpty() || (user.getFullName() + " " + user.getLecturerCode() + " " + user.getEmail()).toLowerCase(Locale.ROOT).contains(query))
                .distinct()
                .map(user -> new ChatContact(user.getId(), user.getLecturerCode(), user.getFullName(), user.getRole().name(), user.getFacultyId(),
                        user.getAcademicRank().name(), user.getAcademicDegree().name(), user.getAvatar()))
                .toList();
    }
    public record ChatContact(UUID userId, String lecturerCode, String displayName, String role, String facultyId,
                              String academicRank, String academicDegree, String avatarUrl) {}
}
