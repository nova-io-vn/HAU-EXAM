package com.userservice.presentation.mapper;

import com.userservice.domain.model.UserProfile;
import com.userservice.presentation.response.UserProfileResponse;
import org.springframework.stereotype.Component;
import com.userservice.domain.repository.FacultyRepository;

import java.time.Clock;

@Component
public class UserProfileResponseMapper {
    private final Clock clock;
    private final FacultyRepository faculties;

    public UserProfileResponseMapper(Clock clock, FacultyRepository faculties) {
        this.clock = clock;
        this.faculties = faculties;
    }

    public UserProfileResponse toResponse(UserProfile u) {
        String facultyName = u.getFacultyId() == null ? null : faculties.findByCode(u.getFacultyId()).map(f -> f.name()).orElse(null);
        return new UserProfileResponse(u.getId(), u.getLecturerCode(), u.getFullName(), u.getDateOfBirth(), u.getDateOfBirth() == null ? null : u.age(clock), u.getPhone(), u.getEmail(), u.getAddress(), u.getAvatar(), u.getAvatarPublicId(), u.getAcademicRank(), u.getAcademicDegree(), u.getFacultyId(), facultyName, u.getRole(), u.getStatus(), u.getCreatedAt(), u.getUpdatedAt());
    }
}
