package com.userservice.application.service;

import com.userservice.application.dto.AudienceMember;
import com.userservice.application.port.in.AudienceQueryUseCase;
import com.userservice.domain.model.Role;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AudienceQueryService implements AudienceQueryUseCase {
    private final UserProfileRepository users;

    public AudienceQueryService(UserProfileRepository users) { this.users = users; }

    @Override
    @Transactional(readOnly = true)
    public List<AudienceMember> resolve(String role, String facultyId) {
        Role parsedRole = role == null || role.isBlank() ? null : Role.valueOf(role.trim());
        if (parsedRole == null && (facultyId == null || facultyId.isBlank())) {
            throw new IllegalArgumentException("Scheduled audience requires role and/or facultyId");
        }
        return users.findActiveAudience(parsedRole, facultyId).stream()
                .map(user -> new AudienceMember(user.getId(), user.getEmail())).toList();
    }
}
