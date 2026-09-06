package com.userservice.presentation.mapper;

import com.userservice.domain.model.UserProfile;
import com.userservice.presentation.response.UserProfileResponse;
import org.springframework.stereotype.Component;
import java.time.Clock;
@Component
public class UserProfileResponseMapper {
    private final Clock clock; public UserProfileResponseMapper(Clock clock){this.clock=clock;}
    public UserProfileResponse toResponse(UserProfile u){return new UserProfileResponse(u.getId(),u.getLecturerCode(),u.getFullName(),u.getDateOfBirth(),u.getDateOfBirth()==null?null:u.age(clock),u.getPhone(),u.getEmail(),u.getAddress(),u.getAvatar(),u.getFacultyId(),u.getRole(),u.getStatus(),u.getCreatedAt(),u.getUpdatedAt());}
}
