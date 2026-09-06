package com.userservice.infrastructure.persistence.repository;

import com.userservice.infrastructure.persistence.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.userservice.domain.model.Role;
import com.userservice.domain.model.UserStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface JpaUserProfileRepository extends JpaRepository<UserProfileEntity,UUID> {
    Optional<UserProfileEntity> findByLecturerCode(String lecturerCode);
    boolean existsByLecturerCode(String lecturerCode);
    boolean existsByEmailIgnoreCase(String email);
    @Query("select u from UserProfileEntity u where u.status = :status " +
            "and (:role is null or u.role = :role) and (:facultyId is null or u.facultyId = :facultyId)")
    List<UserProfileEntity> findAudience(@Param("status") UserStatus status, @Param("role") Role role,
                                         @Param("facultyId") String facultyId);
}
