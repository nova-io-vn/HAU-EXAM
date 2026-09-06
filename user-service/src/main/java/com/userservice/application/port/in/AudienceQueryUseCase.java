package com.userservice.application.port.in;

import com.userservice.application.dto.AudienceMember;

import java.util.List;

public interface AudienceQueryUseCase {
    List<AudienceMember> resolve(String role, String facultyId);
}
