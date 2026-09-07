package com.userservice.application.service;

import com.userservice.application.dto.UserContact;
import com.userservice.application.port.in.UserContactQueryUseCase;
import com.userservice.domain.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserContactQueryService implements UserContactQueryUseCase {
    private final UserProfileRepository users;
    public UserContactQueryService(UserProfileRepository users) { this.users = users; }
    @Override @Transactional(readOnly = true)
    public UserContact find(UUID userId) {
        var user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return new UserContact(user.getId(), user.getLecturerCode(), user.getFullName(), user.getEmail(), user.getFacultyId());
    }
}
