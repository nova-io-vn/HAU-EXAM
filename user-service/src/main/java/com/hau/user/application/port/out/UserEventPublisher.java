package com.hau.user.application.port.out;

import com.hau.user.domain.model.UserProfile;
import java.util.UUID;
public interface UserEventPublisher {
    void userApproved(UserProfile user, UUID correlationId);
    void userRejected(UserProfile user, UUID correlationId);
    void roleChanged(UserProfile user, UUID correlationId);
    void facultyChanged(UserProfile user, UUID correlationId);
    void statusChanged(UserProfile user, UUID correlationId);
}
