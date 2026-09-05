package com.hau.user.application.port.in;

import com.hau.user.application.dto.UpdateProfileCommand;
import com.hau.user.domain.model.UserProfile;
import java.util.UUID;
public interface UserProfileUseCase {
    UserProfile getOwnProfile(UUID authenticatedUserId);
    UserProfile updateOwnProfile(UUID authenticatedUserId, UpdateProfileCommand command);
}
