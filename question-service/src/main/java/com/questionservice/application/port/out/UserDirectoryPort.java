package com.questionservice.application.port.out;

import com.questionservice.application.model.LecturerProfile;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserDirectoryPort {
    Optional<LecturerProfile> findLecturer(UUID userId);
    List<LecturerProfile> findActiveLecturers(Set<String> facultyIds, String keyword);
}
