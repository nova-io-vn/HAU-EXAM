package com.userservice.application.port.in;

import com.userservice.application.dto.UpdateProfileCommand;
import com.userservice.domain.model.UserProfile;
import java.util.UUID;
import com.userservice.application.model.StoredImage;
public interface UserProfileUseCase {
    UserProfile getOwnProfile(UUID authenticatedUserId);
    UserProfile updateOwnProfile(UUID authenticatedUserId, UpdateProfileCommand command);
    UserProfile updateOwnAvatar(UUID authenticatedUserId, StoredImage image);
}
