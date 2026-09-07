package com.userservice.presentation.response;

import com.userservice.domain.model.Role;
import java.time.Instant;

public record OnboardingStateResponse(Role role, int versionCompleted, Instant completedAt) { }
