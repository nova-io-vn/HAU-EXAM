package com.userservice.application.port.in;

import com.userservice.application.dto.UserContact;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserContactQueryUseCase {
    UserContact find(UUID userId);
    List<UserContact> findAll(Set<UUID> userIds);
    List<UserContact> findActiveLecturers(Set<String> facultyIds, String keyword);
}
