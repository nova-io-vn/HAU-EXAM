package com.userservice.domain.repository;

import com.userservice.domain.model.UserProfile;
import com.userservice.domain.model.Role;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserProfileRepository {
    UserProfile save(UserProfile profile);
    Optional<UserProfile> findById(UUID id);
    Optional<UserProfile> findByLecturerCode(String lecturerCode);
    boolean existsById(UUID id);
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByEmail(String email);
    PageResult<UserProfile> findAll(PageQuery query);
    List<UserProfile> findActiveAudience(Role role, String facultyId);
}
